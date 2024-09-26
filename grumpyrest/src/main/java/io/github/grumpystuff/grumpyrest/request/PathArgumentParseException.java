package io.github.grumpystuff.grumpyrest.request;

import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserException;
import io.github.grumpystuff.grumpyrest.response.Response;
import io.github.grumpystuff.grumpyrest.response.SelfResponseFactory;
import io.github.grumpystuff.grumpyrest.response.standard.StandardErrorResponse;

/**
 * This exception type is thrown when a path argument cannot be parsed, wrapping a {@link FromStringParserException}.
 * This indicates that the string to be parsed comes from a path segment (as opposed to a querystring parameter or
 * other string) and sends a standard response for a 404 error. This is needed because in general, parsing
 * a string from an arbitrary source might correspond to a 400 error or an internal (500) error, and might even
 * reveal sensitive information to the client, so no response factory will handle it by default.
 */
public final class PathArgumentParseException extends Exception implements SelfResponseFactory {

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

    @Override
    public Response createResponse() {
        // not using a named field node here because those are meant for JSON fields and the path field names are
        // not even known to the client
        return new StandardErrorResponse(404, getMessage());
    }

}
