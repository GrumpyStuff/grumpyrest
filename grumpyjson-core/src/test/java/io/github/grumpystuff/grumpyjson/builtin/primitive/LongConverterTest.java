/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.primitive;

import io.github.grumpystuff.grumpyjson.json_model.JsonNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.grumpystuff.grumpyjson.JsonTestUtil.*;

public class LongConverterTest {

    private final LongConverter converter = new LongConverter();

    @Test
    public void testDeserializationHappyCase() throws Exception {
        Assertions.assertEquals(0, converter.deserialize(JsonNumber.of(0), Long.TYPE));
        Assertions.assertEquals(123, converter.deserialize(JsonNumber.of(123), Long.TYPE));
        Assertions.assertEquals(-123, converter.deserialize(JsonNumber.of(-123), Long.TYPE));
        Assertions.assertEquals(0x1234567890L, converter.deserialize(JsonNumber.of(0x1234567890L), Long.TYPE));
    }

    @Test
    public void testDeserializationWrongType() throws Exception {
        forNonPrimitive(json -> assertFailsDeserialization(converter, json, Long.TYPE));
        forNull(json -> assertFailsDeserialization(converter, json, Long.TYPE));
        forBooleans(json -> assertFailsDeserialization(converter, json, Long.TYPE));
        forStrings(json -> assertFailsDeserialization(converter, json, Long.TYPE));
    }

    @Test
    public void testDeserializationFloat() {
        assertFailsDeserialization(converter, JsonNumber.of(12.34), Long.TYPE);
    }

    @Test
    public void testSerializationHappyCase() {
        Assertions.assertEquals(JsonNumber.of(123), converter.serialize(123L));
        Assertions.assertEquals(JsonNumber.of(0x1234567890L), converter.serialize(0x1234567890L));
    }

    @Test
    public void testSerializationWithNull() {
        assertFailsSerializationWithNpe(converter, null);
    }

}
