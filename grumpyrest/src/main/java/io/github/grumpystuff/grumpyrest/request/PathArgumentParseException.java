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

    /**
     * the name of the path argument that could not be parsed
     */
    private final String name;

    /**
     * the value of the path argument that could not be parsed
     */
    private final String value;

    /**
     * Constructor
     *
     * @param name the name of the path argument that could not be parsed
     * @param value the value of the path argument that could not be parsed
     * @param cause the exception that caused the parsing to fail
     */
    public PathArgumentParseException(String name, String value, FromStringParserException cause) {
        super(buildMessage(name), cause);
        this.name = name;
        this.value = value;
    }

    private static String buildMessage(String name) {
        return "invalid value for path argument '" + name + "'";
    }

    /**
     * Returns the name of the path argument that could not be parsed.
     *
     * @return the argument name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of the path argument that could not be parsed.
     *
     * @return the argument value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the exception that caused the parsing to fail.
     *
     * @return the cause of the exception
     */
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
