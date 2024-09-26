/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.primitive;

import io.github.grumpystuff.grumpyjson.JsonRegistries;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializer;
import io.github.grumpystuff.grumpyjson.json_model.JsonElement;
import io.github.grumpystuff.grumpyjson.json_model.JsonNumber;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializationException;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A converter for the primitive type int and its boxed type, {@link Integer}.
 * <p>
 * This maps to and from integral JSON numbers in the 32-bit signed integer range.
 * <p>
 * This converter is registered by default, and only needs to be manually registered if it gets removed, such as by
 * calling {@link JsonRegistries#clear()}.
 */
public final class IntegerConverter implements JsonSerializer<Integer>, JsonDeserializer {

    /**
     * Constructor
     */
    public IntegerConverter() {
        // needed to silence Javadoc error because the implicit constructor doesn't have a doc comment
    }

    @Override
    public boolean supportsTypeForDeserialization(Type type) {
        Objects.requireNonNull(type, "type");

        return type.equals(Integer.TYPE) || type.equals(Integer.class);
    }

    @Override
    public Integer deserialize(JsonElement json, Type type) throws JsonDeserializationException {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(type, "type");

        long longValue = IntegralNumberDeserializationUtil.deserialize(json.deserializerExpectsNumber());
        int intValue = (int)longValue;
        IntegralNumberDeserializationUtil.verifyBounds(longValue, intValue);
        return intValue;
    }

    @Override
    public boolean supportsClassForSerialization(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        return clazz.equals(Integer.TYPE) || clazz.equals(Integer.class);
    }

    @Override
    public JsonElement serialize(Integer value) throws JsonSerializationException {
        Objects.requireNonNull(value, "value");

        return JsonNumber.of(value);
    }

}
