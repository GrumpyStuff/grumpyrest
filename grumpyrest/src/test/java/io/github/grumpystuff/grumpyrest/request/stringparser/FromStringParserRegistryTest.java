/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyrest.request.stringparser;

import io.github.grumpystuff.grumpyrest.request.stringparser.standard.IntegerFromStringParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.standard.StringFromStringParser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

public class FromStringParserRegistryTest {

    @Test
    public void testHappyCase() throws Exception {
        FromStringParserRegistry registry = new FromStringParserRegistry();
        registry.register(new StringFromStringParser());
        registry.register(new IntegerFromStringParser());
        registry.seal();

        assertTrue(registry.supports(Integer.TYPE));
        assertTrue(registry.supports(Integer.class));
        assertTrue(registry.supports(String.class));
        assertFalse(registry.supports(Boolean.class));

        assertEquals(5, registry.parseFromString("5", Integer.TYPE));
        assertEquals(5, registry.parseFromString("5", Integer.class));
        assertEquals(5, registry.parseFromString("+5", Integer.class));
        assertEquals(-5, registry.parseFromString("-5", Integer.class));

        assertThrows(FromStringParserException.class, () -> registry.parseFromString(" 5", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("5 ", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("5a", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("a5", Integer.class));

        assertEquals("", registry.parseFromString("", String.class));
        assertEquals("abc", registry.parseFromString("abc", String.class));
        assertEquals("5", registry.parseFromString("5", String.class));
        assertEquals("+5", registry.parseFromString("+5", String.class));
        assertEquals("-5", registry.parseFromString("-5", String.class));
    }

    @Test
    public void testStringNotRegisteredCase() throws Exception {
        FromStringParserRegistry registry = new FromStringParserRegistry();
        registry.register(new IntegerFromStringParser());
        registry.seal();

        assertTrue(registry.supports(Integer.TYPE));
        assertTrue(registry.supports(Integer.class));
        assertFalse(registry.supports(String.class));
        assertFalse(registry.supports(Boolean.class));

        assertEquals(5, registry.parseFromString("5", Integer.TYPE));
        assertEquals(5, registry.parseFromString("5", Integer.class));
        assertEquals(5, registry.parseFromString("+5", Integer.class));
        assertEquals(-5, registry.parseFromString("-5", Integer.class));

        assertThrows(FromStringParserException.class, () -> registry.parseFromString(" 5", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("5 ", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("5a", Integer.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("a5", Integer.class));

        assertThrows(FromStringParserException.class, () -> registry.parseFromString("", String.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("abc", String.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("5", String.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("+5", String.class));
        assertThrows(FromStringParserException.class, () -> registry.parseFromString("-5", String.class));
    }

    @Test
    public void testParserReturnsNull() throws FromStringParserException {
        FromStringParserRegistry registry = new FromStringParserRegistry();
        registry.register(new FromStringParser() {

            public boolean supportsType(Type type) {
                return type == String.class;
            }

            public Object parseFromString(String s, Type type) {
                return s.equals("foo") ? null : s;
            }

            // cannot test "parsing" absent strings in the registry alone

        });
        registry.seal();
        assertThrows(NullPointerException.class, () -> registry.parseFromString("foo", String.class));
        assertEquals("bar", registry.parseFromString("bar", String.class));
    }

}
