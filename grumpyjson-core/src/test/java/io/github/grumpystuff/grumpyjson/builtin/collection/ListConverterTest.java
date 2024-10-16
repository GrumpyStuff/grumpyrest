/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.collection;

import io.github.grumpystuff.grumpyjson.JsonRegistries;
import io.github.grumpystuff.grumpyjson.JsonTestUtil;
import io.github.grumpystuff.grumpyjson.TypeToken;
import io.github.grumpystuff.grumpyjson.builtin.primitive.IntegerConverter;
import io.github.grumpystuff.grumpyjson.builtin.primitive.StringConverter;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;

import static io.github.grumpystuff.grumpyjson.JsonTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

public class ListConverterTest {

    private static final Type INTEGER_LIST_TYPE = new TypeToken<List<Integer>>() {}.getType();
    private static final Type STRING_LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    private final JsonRegistries registries = createRegistries(new IntegerConverter(), new StringConverter());
    private final ListConverter converter = new ListConverter(registries);

    public ListConverterTest() {
        registries.seal();
    }

    @Test
    public void testDeserializationHappyCase() throws Exception {
        assertEquals(List.of(12, 34), converter.deserialize(buildIntArray(12, 34), INTEGER_LIST_TYPE));
        assertEquals(List.of("foo", "bar"), converter.deserialize(buildStringArray("foo", "bar"), STRING_LIST_TYPE));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testUnboundTypeParameter() {

        // This type is not supported because it contains an unbound type parameter. In other words, grumpyjson does
        // not know which type to de-serialize the list elements as. It does not matter whether the type is specified
        // as a class object or as a type token.
        assertFalse(converter.supportsTypeForDeserialization(List.class));
        assertFalse(converter.supportsTypeForDeserialization(new TypeToken<List>() {}.getType()));

        // This is an edge case: the type parameter from List is now bound, but it is bound to a wildcard.
        // The adapter will report this to be supported because we do not want to waste time to recursively check that
        // all contained types are supported -- adapter.supportsType() is meant to select the right adapter to use,
        // not to predict whether there will be any problems when using it. And there *will* be problems, because
        // actually using the adapter like this will try to get the (element) adapter for type "?" from the registry,
        // which does not exist.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<List<?>>() {}.getType()));

        // When the type parameter is bound to a concrete type, then the type is supported.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<List<Integer>>() {}.getType()));

        // Just like the wildcard case, the List adapter does not care if the element type is a concrete type
        // for which there is no adapter.
        assertTrue(converter.supportsTypeForDeserialization(new TypeToken<List<OutputStream>>() {}.getType()));

    }

    @Test
    public void testDeserializationWrongType() throws Exception {
        forNull(json -> assertFailsDeserialization(converter, json, INTEGER_LIST_TYPE));
        forBooleans(json -> assertFailsDeserialization(converter, json, INTEGER_LIST_TYPE));
        forNumbers(json -> assertFailsDeserialization(converter, json, INTEGER_LIST_TYPE));
        forStrings(json -> assertFailsDeserialization(converter, json, INTEGER_LIST_TYPE));
        forObjects(json -> assertFailsDeserialization(converter, json, INTEGER_LIST_TYPE));
    }

    @Test
    public void testDeserializationWrongElementType() {
        assertFailsDeserialization(converter, buildIntArray(12, 34), STRING_LIST_TYPE);
    }

    @Test
    public void testSerializationHappyCase() {
        assertEquals(buildIntArray(), converter.serialize(List.of()));
        assertEquals(buildIntArray(12, 34), converter.serialize(List.of(12, 34)));
        assertEquals(buildStringArray("foo", "bar"), converter.serialize(List.of("foo", "bar")));
    }

    @Test
    public void testSerializationWithNull() {
        assertFailsSerializationWithNpe(converter, null);
    }

    @Test
    public void testElementDeserializerReturnsNull() throws JsonDeserializationException {
        JsonRegistries customRegistries = createRegistries(new JsonTestUtil.PossiblyNullReturningDeserializer());
        ListConverter converter = new ListConverter(customRegistries);
        customRegistries.seal();
        assertEquals(List.of("xxx", "bar"), converter.deserialize(buildStringArray("xxx", "bar"), STRING_LIST_TYPE));
        assertFailsDeserialization(converter, buildStringArray("bar", "foo"), STRING_LIST_TYPE);
    }

    @Test
    public void testElementSerializerReturnsNull() {
        JsonRegistries customRegistries = createRegistries(new JsonTestUtil.PossiblyNullReturningSerializer());
        ListConverter converter = new ListConverter(customRegistries);
        customRegistries.seal();
        assertEquals(buildStringArray("xxx", "bar"), converter.serialize(List.of("xxx", "bar")));
        assertFailsSerialization(converter, List.of("bar", "foo"));
    }

}
