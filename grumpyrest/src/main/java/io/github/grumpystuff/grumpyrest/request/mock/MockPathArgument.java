package io.github.grumpystuff.grumpyrest.request.mock;

/**
 * The name is optional, to be used in case the endpoint under test or the test itself need it.
 * Pass null to generate a name based on the parameter index.
 *
 * @param name the name or null
 * @param value the value
 */
public record MockPathArgument(String name, String value) {

    public MockPathArgument {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    public MockPathArgument(String value) {
        this(null, value);
    }

}
