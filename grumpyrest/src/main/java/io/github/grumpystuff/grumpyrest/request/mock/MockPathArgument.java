package io.github.grumpystuff.grumpyrest.request.mock;

import io.github.grumpystuff.grumpyrest.request.PathArgument;
import io.github.grumpystuff.grumpyrest.request.stringparser.ParseFromStringService;

/**
 * This record passes the value and optionally the name of a path argument to a mock request. It is used instead of
 * {@link PathArgument} because the latter would require passing the {@link ParseFromStringService} in the
 * constructor, making it much more cumbersome to use in tests.
 * <p>
 * The name is optional, to be used in case the endpoint under test or the test itself need it.
 * Pass null to generate a name based on the parameter index.
 *
 * @param name the name or null
 * @param value the value
 */
public record MockPathArgument(String name, String value) {

    /**
     * Standard constructor -- see class comment for details.
     */
    public MockPathArgument {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    /**
     * Value-only constructor that generates a name based on the parameter index.
     *
     * @param value the value
     */
    public MockPathArgument(String value) {
        this(null, value);
    }

}
