/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.jackson;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.github.grumpystuff.grumpyjson.FieldErrorNode;
import io.github.grumpystuff.grumpyjson.JsonEngine;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializationException;
import io.github.grumpystuff.grumpyjson.util.CloseShieldReader;
import io.github.grumpystuff.grumpyjson.util.CloseShieldWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * GSON-based implementation of {@link JsonEngine}.
 */
public abstract class JacksonBasedJsonEngine extends JsonEngine {

    /**
     * Creates a new JSON engine with standard converters registered.
     */
    public JacksonBasedJsonEngine() {
    }

    // -----------------------------------------------------------------------
    // deserialize
    // -----------------------------------------------------------------------

    @Override
    public Object deserialize(Reader source, Type type) throws JsonDeserializationException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");

        source = new CloseShieldReader(source);

        try {
            JsonNode jsonNode = readJson(source);
            if (jsonNode == null || jsonNode.getNodeType() == JsonNodeType.MISSING) {
                // this happens if the source does not even contain malformed JSON, but just nothing (EOF)
                throw new JsonDeserializationException("no JSON to deserialize");
            }
            return deserialize(JacksonTreeMapper.mapFromJackson(jsonNode), type);
        } catch (JsonDeserializationException e) {
            throw mapDeserializationException(e);
        }
    }

    /**
     * This method transforms the error message so it does not reveal too much internals.
     */
    private static JsonDeserializationException mapDeserializationException(JsonDeserializationException exception) {
        Objects.requireNonNull(exception, "exception");

        if (exception.getFieldErrorNode() instanceof FieldErrorNode.InternalException internalExceptionNode) {
            Exception wrappedException = internalExceptionNode.getException();
            if (wrappedException instanceof JsonParseException jsonParseException) {
                var location = jsonParseException.getLocation();
                var message = "syntax error in JSON at line " + location.getLineNr() + ", column " + location.getColumnNr();
                return new JsonDeserializationException(message);
            }
        }
        return exception;
    }

    // -----------------------------------------------------------------------
    // stringify / writeTo
    // -----------------------------------------------------------------------

    @Override
    public void writeTo(Object value, Writer destination) throws JsonSerializationException {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(destination, "destination");

        destination = new CloseShieldWriter(destination);

        writeJson(JacksonTreeMapper.mapToJackson(toJsonElement(value)), destination);
    }

    // -----------------------------------------------------------------------
    // back-ends
    // -----------------------------------------------------------------------

    protected abstract JsonNode readJson(Reader source) throws JsonDeserializationException;
    protected abstract void writeJson(JsonNode json, Writer destination) throws JsonSerializationException;

    public static JacksonBasedJsonEngine fromObjectMapper(ObjectMapper objectMapper) {
        return new JacksonBasedJsonEngine() {

            @Override
            protected JsonNode readJson(Reader source) throws JsonDeserializationException {
                try {
                    return objectMapper.readTree(source);
                } catch (IOException e) {
                    throw new JsonDeserializationException(e);
                }
            }

            @Override
            protected void writeJson(JsonNode json, Writer destination) throws JsonSerializationException {
                try {
                    objectMapper.writeValue(destination, json);
                } catch (IOException e) {
                    throw new JsonSerializationException(e);
                }
            }

        };
    }

    public static JacksonBasedJsonEngine fromObjectReaderAndWriter(ObjectReader objectReader, ObjectWriter objectWriter) {
        return new JacksonBasedJsonEngine() {

            @Override
            protected JsonNode readJson(Reader source) throws JsonDeserializationException {
                try {
                    return objectReader.readTree(source);
                } catch (IOException e) {
                    throw new JsonDeserializationException(e);
                }
            }

            @Override
            protected void writeJson(JsonNode json, Writer destination) throws JsonSerializationException {
                try {
                    objectWriter.writeValue(destination, json);
                } catch (IOException e) {
                    throw new JsonSerializationException(e);
                }
            }

        };
    }

    public static JacksonBasedJsonEngine fromObjectReader(ObjectReader objectReader) {
        return new JacksonBasedJsonEngine() {

            @Override
            protected JsonNode readJson(Reader source) throws JsonDeserializationException {
                try {
                    return objectReader.readTree(source);
                } catch (IOException e) {
                    throw new JsonDeserializationException(e);
                }
            }

            @Override
            protected void writeJson(JsonNode json, Writer destination) {
                throw new UnsupportedOperationException("no ObjectWriter");
            }

        };
    }

    public static JacksonBasedJsonEngine fromObjectWriter(ObjectWriter objectWriter) {
        return new JacksonBasedJsonEngine() {

            @Override
            protected JsonNode readJson(Reader source) {
                throw new UnsupportedOperationException("no ObjectReader");
            }

            @Override
            protected void writeJson(JsonNode json, Writer destination) throws JsonSerializationException {
                try {
                    objectWriter.writeValue(destination, json);
                } catch (IOException e) {
                    throw new JsonSerializationException(e);
                }
            }

        };
    }

}
