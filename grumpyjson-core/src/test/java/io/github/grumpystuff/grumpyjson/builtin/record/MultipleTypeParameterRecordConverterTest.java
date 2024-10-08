/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyjson.builtin.record;

import io.github.grumpystuff.grumpyjson.JsonRegistries;
import io.github.grumpystuff.grumpyjson.builtin.primitive.IntegerConverter;
import io.github.grumpystuff.grumpyjson.builtin.primitive.StringConverter;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializer;
import io.github.grumpystuff.grumpyjson.json_model.JsonNumber;
import io.github.grumpystuff.grumpyjson.json_model.JsonObject;
import io.github.grumpystuff.grumpyjson.json_model.JsonString;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.github.grumpystuff.grumpyjson.JsonTestUtil.createRegistries;

/**
 * This test ensures that handling multiple type parameters works. We also swap their order.
 */
public class MultipleTypeParameterRecordConverterTest {

    private record Inner<A, B>(A a, B b) {}
    private record Middle<P, Q>(Inner<Q, P> inner) {}
    private record Outer(Middle<Integer, String> middle) {}

    private final JsonSerializer<Outer> serializer;
    private final JsonDeserializer deserializer;

    public MultipleTypeParameterRecordConverterTest() throws Exception {
        JsonRegistries registries = createRegistries(new IntegerConverter(), new StringConverter());
        registries.seal();
        serializer = registries.getSerializer(Outer.class);
        deserializer = registries.getDeserializer(Outer.class);
    }

    @Test
    public void testHappyCase() throws Exception {
        JsonObject innerJson = JsonObject.of("a", JsonString.of("foo"), "b", JsonNumber.of(12));
        JsonObject middleJson = JsonObject.of("inner", innerJson);
        JsonObject outerJson = JsonObject.of("middle", middleJson);

        Inner<String, Integer> innerRecord = new Inner<>("foo", 12);
        Middle<Integer, String> middleRecord = new Middle<>(innerRecord);
        Outer outerRecord = new Outer(middleRecord);

        Assertions.assertEquals(outerRecord, deserializer.deserialize(outerJson, Outer.class));
        Assertions.assertEquals(outerJson, serializer.serialize(outerRecord));
    }

}
