/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.collection;

import io.github.grumpystuff.grumpyjson.JsonRegistries;
import io.github.grumpystuff.grumpyjson.TypeToken;
import io.github.grumpystuff.grumpyjson.builtin.primitive.IntegerConverter;
import io.github.grumpystuff.grumpyjson.builtin.primitive.StringConverter;
import io.github.grumpystuff.grumpyjson.json_model.JsonNumber;
import io.github.grumpystuff.grumpyjson.json_model.JsonObject;
import io.github.grumpystuff.grumpyjson.json_model.JsonString;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Map;

import static io.github.grumpystuff.grumpyjson.JsonTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class MapConverterTest {

    private static final Type STRING_INTEGER_MAP_TYPE = new TypeToken<Map<String, Integer>>() {}.getType();
    private static final Type STRING_STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static final Type INTEGER_STRING_MAP_TYPE = new TypeToken<Map<Integer, String>>() {}.getType();

    private final JsonRegistries registries = createRegistries(new IntegerConverter(), new StringConverter());
    private final MapConverter converter = new MapConverter(registries);

    public MapConverterTest() {
        registries.seal();
    }

    @Test
    public void testDeserializationHappyCase() throws Exception {

        var stringIntegerObject = JsonObject.of("foo", JsonNumber.of(12), "bar", JsonNumber.of(34));
        assertEquals(Map.of("foo", 12, "bar", 34), converter.deserialize(stringIntegerObject, STRING_INTEGER_MAP_TYPE));

        var stringStringObject = JsonObject.of("foo", JsonString.of("aa"), "bar", JsonString.of("bb"));
        assertEquals(Map.of("foo", "aa", "bar", "bb"), converter.deserialize(stringStringObject, STRING_STRING_MAP_TYPE));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testUnboundTypeParameter() {

        // This type is not supported because it contains an unbound type parameter. In other words, grumpyjson does
        // not know which types to de-serialize the map keys and values as. It does not matter whether the type is
        // specified as a class object or as a type token.
        assertFalse(converter.supportsTypeForDeserialization(Map.class));
        assertFalse(converter.supportsTypeForDeserialization(new TypeToken<Map>() {}.getType()));

        // This is an edge case: the type parameters from Map are now bound, but they are bound to a wildcard.
        // The adapter will report this to be supported because we do not want to waste time to recursively check that
        // all contained types are supported -- adapter.supportsType() is meant to select the right adapter to use,
        // not to predict whether there will be any problems when using it. And there *will* be problems, because
        // actually using the adapter like this will try to get the (key or value) adapter for type "?" from the
        // registry, which does not exist.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<?, String>>() {}.getType()));
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<String, ?>>() {}.getType()));
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<?, ?>>() {}.getType()));

        // When the type parameter is bound to a concrete type, then the type is supported.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<String, String>>() {}.getType()));

        // Just like the wildcard case, the List adapter does not care if the element type is a concrete type
        // for which there is no adapter.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<OutputStream, String>>() {}.getType()));
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<String, OutputStream>>() {}.getType()));
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<Map<OutputStream, OutputStream>>() {}.getType()));

    }

    @Test
    public void testDeserializationWrongType() throws Exception {
        forNull(json -> assertFailsDeserialization(converter, json, STRING_STRING_MAP_TYPE));
        forBooleans(json -> assertFailsDeserialization(converter, json, STRING_STRING_MAP_TYPE));
        forNumbers(json -> assertFailsDeserialization(converter, json, STRING_STRING_MAP_TYPE));
        forStrings(json -> assertFailsDeserialization(converter, json, STRING_STRING_MAP_TYPE));
        forArrays(json -> assertFailsDeserialization(converter, json, STRING_STRING_MAP_TYPE));
    }

    @Test
    public void testDeserializationWrongElementType() {

        var stringIntegerObject = JsonObject.of("foo", JsonNumber.of(12), "bar", JsonNumber.of(34));
        assertFailsDeserialization(converter, stringIntegerObject, STRING_STRING_MAP_TYPE);

        var stringStringObject = JsonObject.of("foo", JsonString.of("aa"), "bar", JsonString.of("bb"));
        assertFailsDeserialization(converter, stringStringObject, STRING_INTEGER_MAP_TYPE);

        // this fails because the keys are formally strings, even though they contain numeric digits
        var integerIntegerObject = JsonObject.of("1", JsonString.of("aa"), "2", JsonString.of("bb"));
        assertFailsDeserialization(converter, integerIntegerObject, INTEGER_STRING_MAP_TYPE);

    }

    @Test
    public void testSerializationHappyCase() {

        assertEquals(JsonObject.of(), converter.serialize(Map.of()));

        var stringIntegerObject = JsonObject.of("foo", JsonNumber.of(12), "bar", JsonNumber.of(34));
        assertEquals(stringIntegerObject, converter.serialize(Map.of("foo", 12, "bar", 34)));

        var stringStringObject = JsonObject.of("foo", JsonString.of("aa"), "bar", JsonString.of("bb"));
        assertEquals(stringStringObject, converter.serialize(Map.of("foo", "aa", "bar", "bb")));

    }

    @Test
    public void testSerializationWithNull() {
        assertFailsSerializationWithNpe(converter, null);
    }

}
