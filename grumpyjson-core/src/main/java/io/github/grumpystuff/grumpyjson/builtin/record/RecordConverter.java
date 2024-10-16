/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.record;

import io.github.grumpystuff.grumpyjson.ExceptionMessages;
import io.github.grumpystuff.grumpyjson.FieldErrorNode;
import io.github.grumpystuff.grumpyjson.JsonProviders;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializer;
import io.github.grumpystuff.grumpyjson.json_model.JsonElement;
import io.github.grumpystuff.grumpyjson.json_model.JsonObject;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializationException;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializer;
import io.github.grumpystuff.grumpyjson.util.NullReturnCheckingCalls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Maps a record class to and from a JSON object.
 * <p>
 * A RecordConverter is built for the raw type (class) of a record, so a single instance handles all
 * parameterized types for that raw type.
 * <p>
 * Serialization is based on the run-time classes of all values and so is straightforward.
 * <p>
 * For deserialization, at run time (potential optimization: at selection time) the deserializer is used for a concrete
 * parameterized type. This type must be concrete in the sense that it cannot contain type variables anymore (nor
 * wildcards -- we do not support those anyway). If the field that uses the record type *did* use type variables, then
 * these must have been replaced by concrete types before passing on to this deserializer. So at this point the record
 * type has an ordered list of named type parameters, and the concrete type binds them to an ordered list of concrete
 * type arguments.
 * <p>
 * This deserializer then goes through the record fields. Each field potentially uses type variables, and all these
 * must have been declared as type parameters by the record -- type variables (as opposed to the types they are bound
 * to) do not cross the boundaries of a single record definition, and fields cannot define their own type variables.
 * <p>
 * For each record field, the field type is "concretized" -- replaced by a like-structured type in which type variables
 * have been replaced by the types they are bound to. A single type variable is looked up in the record's type
 * parameters by name, then the type argument at the same index is bound to the variable.
 * <p>
 * Finally, the fields get deserialized from the JSON fields using the deserializers for the resulting concrete types.
 *
 * @param <T> the record type
 */
public final class RecordConverter<T> implements JsonSerializer<T>, JsonDeserializer {

    /**
     * Controls the behavior of a {@link RecordConverter}.
     *
     * @param ignoreUnknownProperties to ignore unknown properties during deserialization instead of throwing an exception
     */
    public record Options(boolean ignoreUnknownProperties) {
    }

    private final RecordInfo recordInfo;
    private final JsonProviders providers;
    private final Options options;

    /**
     * Application code usually does not have to call this constructor because instances of this class will be
     * auto-generated for unknown records, and this constructor does not add any features on top of that.
     *
     * @param clazz      the record class
     * @param providers the JSON providers -- needed to fetch the converters for field types at run-time
     */
    public RecordConverter(Class<T> clazz, JsonProviders providers) {
        this(clazz, providers, new Options(false));
    }

    /**
     * This constructor adds extra options. Application code can create custom record converters manually using this
     * constructor in cases where the auto-generated ones are not sufficient.
     *
     * @param clazz      the record class
     * @param providers the JSON providers -- needed to fetch the converters for field types at run-time
     * @param options    options that control the conversion from and to JSON
     */
    public RecordConverter(Class<T> clazz, JsonProviders providers, Options options) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(providers, "providers");
        Objects.requireNonNull(options, "options");

        this.recordInfo = new RecordInfo(clazz);
        this.providers = providers;
        this.options = options;
    }

    @Override
    public boolean supportsTypeForDeserialization(Type type) {
        Objects.requireNonNull(type, "type");

        if (type instanceof Class<?>) {
            return type.equals(recordInfo.getRecordClass());
        } else if (type instanceof ParameterizedType p && p.getRawType() instanceof Class<?>) {
            return p.getRawType().equals(recordInfo.getRecordClass());
        } else {
            return false;
        }
    }

    @Override
    public T deserialize(JsonElement json, Type recordType) throws JsonDeserializationException {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(recordType, "recordType");

        Map<String, JsonElement> jsonProperties = json.deserializerExpectsObject();
        List<RecordInfo.ComponentInfo> componentInfos = recordInfo.getComponentInfos();
        int numberOfPresentKnownProperties = 0;
        Object[] fieldValues = new Object[componentInfos.size()];
        FieldErrorNode errorNode = null;

        for (int i = 0; i < componentInfos.size(); i++) {
            RecordInfo.ComponentInfo componentInfo = componentInfos.get(i);
            String name = componentInfo.getName();
            JsonElement propertyJson = jsonProperties.get(name);
            if (propertyJson != null) {
                numberOfPresentKnownProperties++;
            }
            try {
                Type concreteFieldType = componentInfo.getConcreteType(recordType);
                JsonDeserializer deserializer = providers.getDeserializer(concreteFieldType);
                if (propertyJson == null) {
                    fieldValues[i] = NullReturnCheckingCalls.deserializeAbsent(deserializer, concreteFieldType);
                } else {
                    fieldValues[i] = NullReturnCheckingCalls.deserialize(deserializer, propertyJson, concreteFieldType);
                }
            } catch (JsonDeserializationException e) {
                errorNode = e.getFieldErrorNode().in(name).and(errorNode);
            } catch (Exception e) {
                errorNode = FieldErrorNode.create(e).in(name).and(errorNode);
            }
        }

        // jsonObject.size() counts the present properties; numberOfPresentKnownProperties counts the present
        // *known* properties. So they differ iff there is at least one present unknown property.
        if (!options.ignoreUnknownProperties() && numberOfPresentKnownProperties != jsonProperties.size()) {
            // this is more expensive, so only do this if there is really an error
            Set<String> propertyNames = new HashSet<>(jsonProperties.keySet());
            for (RecordInfo.ComponentInfo componentInfo : componentInfos) {
                propertyNames.remove(componentInfo.getName());
            }
            for (String unexpectedProperty : propertyNames) {
                errorNode = FieldErrorNode.create(ExceptionMessages.UNEXPECTED_PROPERTY).in(unexpectedProperty).and(errorNode);
            }
        }

        if (errorNode != null) {
            throw new JsonDeserializationException(errorNode);
        }
        try {
            //noinspection unchecked
            return (T) recordInfo.invokeConstructor(fieldValues);
        } catch (InvocationTargetException e) {
            // Since records are considered data containers, we expect exceptions from a record constructor to be
            // related to the record arguments, which we know. So returning the exception message in the response
            // should not leak any sensitive information. This allows error messages related to the *combination*
            // of multiple fields to be visible in the response without writing any custom code.
            throw new JsonDeserializationException(FieldErrorNode.create(e.getTargetException().getMessage()));
        } catch (Exception e) {
            throw new JsonDeserializationException(FieldErrorNode.create(e));
        }
    }

    @Override
    public boolean supportsClassForSerialization(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        return clazz.equals(recordInfo.getRecordClass());
    }

    @Override
    public JsonElement serialize(T record) {
        Objects.requireNonNull(record, "value"); // called value in the interface

        Map<String, JsonElement> jsonProperties = new HashMap<>();
        FieldErrorNode errorNode = null;
        for (RecordInfo.ComponentInfo componentInfo : recordInfo.getComponentInfos()) {
            String name = componentInfo.getName();
            try {
                Object value = componentInfo.invokeGetter(record);
                if (value == null) {
                    throw new JsonSerializationException("field is null");
                }
                Optional<JsonElement> optionalJson = providers.serializeOptional(value);
                optionalJson.ifPresent(jsonElement -> jsonProperties.put(name, jsonElement));
            } catch (JsonSerializationException e) {
                errorNode = e.getFieldErrorNode().in(name).and(errorNode);
            } catch (Exception e) {
                errorNode = FieldErrorNode.create(e).in(name).and(errorNode);
            }
        }
        if (errorNode != null) {
            throw new JsonSerializationException(errorNode);
        }
        return JsonObject.of(jsonProperties);
    }

}
