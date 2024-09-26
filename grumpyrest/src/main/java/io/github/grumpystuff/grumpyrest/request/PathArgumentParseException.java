package io.github.grumpystuff.grumpyrest.request;

import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserException;

/**
 * This exception type is thrown when a path argument cannot be parsed, wrapping a {@link FromStringParserException}.
 * This indicates that the string to be parsed comes from a path segment (as opposed to a querystring parameter or
 * other string) and triggers a standard response factory for a 404 error. This is needed because in general, parsing
 * a string from an arbitrary source might correspond to a 400 error or an internal (500) error, and might even
 * reveal sensitive information to the client, so no response factory will handle it by default.
 */
public final class PathArgumentParseException extends Exception {

    private final String name;
    private final String value;

    public PathArgumentParseException(String name, String value, FromStringParserException cause) {
        super(buildMessage(name), cause);
        this.name = name;
        this.value = value;
    }

    private static String buildMessage(String name) {
        return "invalid value for path argument '" + name + "'";
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public FromStringParserException getCause() {
        return (FromStringParserException) super.getCause();
    }

}
