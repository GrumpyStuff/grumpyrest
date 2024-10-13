package io.github.grumpystuff.grumpyrest.request.mock;

import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.json_model.JsonElement;
import io.github.grumpystuff.grumpyjson.registry.NotRegisteredException;
import io.github.grumpystuff.grumpyrest.request.PathArgument;
import io.github.grumpystuff.grumpyrest.request.Request;
import io.github.grumpystuff.grumpyrest.request.querystring.QuerystringParsingException;
import io.github.grumpystuff.grumpyrest.response.FinishRequestException;
import io.github.grumpystuff.grumpyrest.response.standard.StandardErrorResponse;
import io.github.grumpystuff.grumpyrest.util.NullReturnCheckingCalls;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A mock implementation of {@link Request} that can be used in tests.
 */
public final class MockRequest implements Request {

    private final MockRequestServices services;
    private String method;
    private Map<String, String> headers;
    private List<PathArgument> pathArguments;
    private Map<String, String> querystring;
    private JsonElement body;

    /**
     * Constructor.
     *
     * @param services the services used to parse path arguments, querystring arguments, and request bodies
     */
    public MockRequest(MockRequestServices services) {
        this.services = services;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // configuration
    // ----------------------------------------------------------------------------------------------------------------

    /**
     * Sets the HTTP method. By default, no method is set, and trying to get the method will cause an
     * {@link IllegalStateException}.
     * <p>
     * Setting the request method is usually not needed in single-endpoint tests because the method is used to select
     * an endpoint from the API, but not used by the endpoint itself.
     *
     * @param method the HTTP method
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Sets HTTP headers. By default, no headers are set, and trying to get a header will cause an
     * {@link IllegalStateException}.
     *
     * @param headers the HTTP headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = Map.copyOf(headers);
    }

    /**
     * Sets path arguments. By default, no path arguments are set, and trying to get a path argument will cause an
     * {@link IllegalStateException}.
     * <p>
     * See {@link MockPathArgument} for information on why it is used instead of {@link PathArgument}.
     *
     * @param pathArguments the path arguments
     */
    public void setPathArguments(List<MockPathArgument> pathArguments) {
        List<PathArgument> result = new ArrayList<>();
        for (int i = 0; i < pathArguments.size(); i++) {
            MockPathArgument argument = pathArguments.get(i);
            String name = argument.name() == null ? "arg" + i : argument.name();
            result.add(new PathArgument(name, argument.value(), services.fromStringParserRegistry()));
        }
        this.pathArguments = List.copyOf(result);
    }

    /**
     * Sets the querystring. By default, no querystring is set, and trying to obtain the querystring will cause an
     * {@link IllegalStateException}.
     *
     * @param querystring the querystring
     */
    public void setQuerystring(Map<String, String> querystring) {
        this.querystring = Map.copyOf(querystring);
    }

    /**
     * Sets the request body. By default, no body is present, and trying to parse the body will cause an
     * {@link IllegalStateException}.
     *
     * @param body the request body
     */
    public void setBody(JsonElement body) {
        this.body = body;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // interface
    // ----------------------------------------------------------------------------------------------------------------

    @Override
    public String getMethod() {
        if (method == null) {
            throw new IllegalStateException("No method set");
        }
        return method;
    }

    @Override
    public String getHeader(String name) {
        if (headers == null) {
            throw new IllegalStateException("No headers set");
        }
        return headers.get(name);
    }

    @Override
    public List<PathArgument> getPathArguments() {
        if (pathArguments == null) {
            throw new IllegalStateException("No path arguments set");
        }
        return pathArguments;
    }

    @Override
    public Object parseQuerystring(Type type) throws QuerystringParsingException {
        Objects.requireNonNull(type, "type");

        if (querystring == null) {
            throw new IllegalStateException("No querystring set");
        }
        try {
            var parser = services.querystringParserRegistry().get(type);
            return NullReturnCheckingCalls.parse(parser, querystring, type);
        } catch (NotRegisteredException e) {
            throw new QuerystringParsingException(Map.of("(root)", e.getMessage()));
        }
    }

    public Object parseBody(Type type) {
        Objects.requireNonNull(type, "type");

        if (body == null) {
            throw new IllegalStateException("No body set");
        }
        try {
            return services.jsonEngine().deserialize(body, type);
        } catch (JsonDeserializationException e) {
            throw new FinishRequestException(StandardErrorResponse.requestBodyValidationFailed(e));
        }
    }

}
