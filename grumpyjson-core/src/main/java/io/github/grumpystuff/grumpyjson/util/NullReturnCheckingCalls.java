package io.github.grumpystuff.grumpyjson.util;

import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializer;
import io.github.grumpystuff.grumpyjson.json_model.JsonElement;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializationException;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * NOT PUBLIC API
 */
public class NullReturnCheckingCalls {

    // prevent instantiation
    private NullReturnCheckingCalls() {
    }

    /**
     * NOT PUBLIC API
     *
     * @param deserializer ...
     * @param json ...
     * @param type ...
     * @return ...
     * @throws JsonDeserializationException ...
     */
    public static Object deserialize(JsonDeserializer deserializer, JsonElement json, Type type) throws JsonDeserializationException {
        return Objects.requireNonNull(deserializer.deserialize(json, type), "deserializer.deserialize() returned null");
    }

    /**
     * NOT PUBLIC API
     *
     * @param deserializer ...
     * @param type ...
     * @return ...
     * @throws JsonDeserializationException ...
     */
    public static Object deserializeAbsent(JsonDeserializer deserializer, Type type) throws JsonDeserializationException {
        return Objects.requireNonNull(deserializer.deserializeAbsent(type), "deserializer.deserializeAbsent() returned null");
    }

    /**
     * NOT PUBLIC API
     *
     * @param serializer ...
     * @param value ...
     * @return ...
     * @param <T> ...
     * @throws JsonSerializationException ...
     */
    public static <T> JsonElement serialize(JsonSerializer<T> serializer, T value) throws JsonSerializationException {
        return Objects.requireNonNull(serializer.serialize(value), "serializer.serialize() returned null");
    }

    /**
     * NOT PUBLIC API
     *
     * @param serializer ...
     * @param value ...
     * @return ...
     * @param <T> ...
     * @throws JsonSerializationException ...
     */
    public static <T> Optional<JsonElement> serializeOptional(JsonSerializer<T> serializer, T value) throws JsonSerializationException {
        return Objects.requireNonNull(serializer.serializeOptional(value), "serializer.serializeOptional() returned null");
    }

}
