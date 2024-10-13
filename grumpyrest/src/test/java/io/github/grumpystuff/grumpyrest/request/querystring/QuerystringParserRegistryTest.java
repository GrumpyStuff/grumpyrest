/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyrest.request.querystring;

import io.github.grumpystuff.grumpyjson.builtin.helper_types.OptionalField;
import io.github.grumpystuff.grumpyjson.registry.NotRegisteredException;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserRegistry;
import io.github.grumpystuff.grumpyrest.request.stringparser.standard.IntegerFromStringParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.standard.OptionalFieldParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.standard.StringFromStringParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuerystringParserRegistryTest {

    private static final FromStringParserRegistry fromStringParserRegistry = new FromStringParserRegistry();
    static {
        fromStringParserRegistry.register(new StringFromStringParser());
        fromStringParserRegistry.register(new IntegerFromStringParser());
        fromStringParserRegistry.register(new OptionalFieldParser(fromStringParserRegistry));
        fromStringParserRegistry.seal();
    }

    @Test
    public void testRecord() throws Exception {
        record Foo(int x, String y) {}
        QuerystringParserRegistry registry = new QuerystringParserRegistry(fromStringParserRegistry);
        registry.seal();
        Assertions.assertEquals(
            new Foo(5, "abc"),
            registry.get(Foo.class).parse(Map.of("x", "5", "y", "abc"), Foo.class)
        );
        Assertions.assertThrows(
            QuerystringParsingException.class,
            () -> registry.get(Foo.class).parse(Map.of("x", "5 ", "y", "abc"), Foo.class)
        );
        Assertions.assertThrows(
            QuerystringParsingException.class,
            () -> registry.get(Foo.class).parse(Map.of("x", "5"), Foo.class)
        );
        Assertions.assertThrows(
            QuerystringParsingException.class,
            () -> registry.get(Foo.class).parse(Map.of("x", "5", "y", "abc", "z", "zzz"), Foo.class)
        );
    }

    @Test
    public void testRecordWithOptionalField() throws Exception {
        record Foo(int x, OptionalField<String> y) {}
        QuerystringParserRegistry registry = new QuerystringParserRegistry(fromStringParserRegistry);
        registry.seal();
        Assertions.assertEquals(
            new Foo(5, OptionalField.ofValue("abc")),
            registry.get(Foo.class).parse(Map.of("x", "5", "y", "abc"), Foo.class)
        );
        Assertions.assertThrows(
            QuerystringParsingException.class,
            () -> registry.get(Foo.class).parse(Map.of("x", "5 ", "y", "abc"), Foo.class)
        );
        Assertions.assertEquals(
            new Foo(5, OptionalField.ofNothing()),
            registry.get(Foo.class).parse(Map.of("x", "5"), Foo.class)
        );
        Assertions.assertThrows(
            QuerystringParsingException.class,
            () -> registry.get(Foo.class).parse(Map.of("x", "5", "y", "abc", "z", "zzz"), Foo.class)
        );
    }

    @Test
    public void testParserReturnsNull() throws QuerystringParsingException, NotRegisteredException {
        FromStringParserRegistry customFromStringParserRegistry = new FromStringParserRegistry();
        customFromStringParserRegistry.register(new FromStringParser() {

            public boolean supportsType(Type type) {
                return type == String.class;
            }

            public Object parseFromString(String s, Type type) {
                return s.equals("foo") ? null : s;
            }

            @Override
            public Object parseFromAbsentString(Type type) {
                return null;
            }

        });
        customFromStringParserRegistry.seal();
        QuerystringParserRegistry querystringParserRegistry = new QuerystringParserRegistry(customFromStringParserRegistry);
        querystringParserRegistry.seal();

        record Parameters(String s) {}
        assertEquals(
                new Parameters("xxx"),
                querystringParserRegistry.get(Parameters.class).parse(Map.of("s", "xxx"), Parameters.class)
        );
        assertThrows(
                QuerystringParsingException.class,
                () -> querystringParserRegistry.get(Parameters.class).parse(Map.of("s", "foo"), Parameters.class)
        );
        assertThrows(
                QuerystringParsingException.class,
                () -> querystringParserRegistry.get(Parameters.class).parse(Map.of(), Parameters.class)
        );
    }

}
