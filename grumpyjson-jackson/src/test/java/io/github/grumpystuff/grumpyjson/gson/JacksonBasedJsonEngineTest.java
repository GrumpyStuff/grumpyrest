/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.gson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.grumpystuff.grumpyjson.JsonEngine;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.json_model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JacksonBasedJsonEngineTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonEngine engine = JacksonBasedJsonEngine.fromObjectMapper(objectMapper);

    public JacksonBasedJsonEngineTest() {
        engine.seal();
    }

    @Test
    public void testNullToJson() {
        Assertions.assertThrows(NullPointerException.class, () -> engine.serializeToString(null));
        Assertions.assertThrows(NullPointerException.class, () -> engine.deserialize((String)null, String.class));
    }

    @Test
    public void testPrimitives() throws JsonDeserializationException {
        Assertions.assertEquals("null", engine.serializeToString(JsonNull.of()));
        Assertions.assertEquals("false", engine.serializeToString(false));
        Assertions.assertEquals("123", engine.serializeToString(123));
        Assertions.assertEquals("\"foo\"", engine.serializeToString("foo"));

        Assertions.assertEquals(JsonNull.of(), engine.deserialize("null", JsonElement.class));
        Assertions.assertEquals(JsonBoolean.of(true), engine.deserialize("true", JsonElement.class));
        Assertions.assertEquals(JsonNumber.of(123), engine.deserialize("123", JsonElement.class));
        Assertions.assertEquals(JsonString.of("foo"), engine.deserialize("\"foo\"", JsonElement.class));
    }

    @Test
    public void testSyntaxError() {
        var exception = Assertions.assertThrows(
                JsonDeserializationException.class,
                () -> engine.deserialize("[\n123,\n]", JsonElement.class)
        );
        Assertions.assertTrue(exception.getMessage().contains("line 3"));
        Assertions.assertTrue(exception.getMessage().contains("column 1"));
    }

}
