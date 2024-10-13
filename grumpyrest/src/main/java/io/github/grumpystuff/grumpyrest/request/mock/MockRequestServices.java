package io.github.grumpystuff.grumpyrest.request.mock;

import io.github.grumpystuff.grumpyjson.JsonEngine;
import io.github.grumpystuff.grumpyjson.StructuralJsonEngine;
import io.github.grumpystuff.grumpyrest.request.querystring.QuerystringParserRegistry;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserRegistry;

/**
 * Implements the infrastructure behind {@link MockRequest}s, such as the {@link JsonEngine} to use.
 */
public record MockRequestServices(
        FromStringParserRegistry fromStringParserRegistry,
        QuerystringParserRegistry querystringParserRegistry,
        StructuralJsonEngine jsonEngine
) {

    // only needed because we can't have code before the super-constructor call
    private MockRequestServices(FromStringParserRegistry fromStringParserRegistry) {
        this(fromStringParserRegistry, new QuerystringParserRegistry(fromStringParserRegistry), new StructuralJsonEngine());
    }

    public MockRequestServices() {
        this(new FromStringParserRegistry());
    }

}
