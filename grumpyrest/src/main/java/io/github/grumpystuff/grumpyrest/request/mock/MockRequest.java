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

public class MockRequest implements Request {

    private final MockRequestServices services;
    private String method = "UNKNOWN";
    private Map<String, String> headers = Map.of();
    private List<PathArgument> pathArguments = List.of();
    private Map<String, String> querystring = Map.of();
    private JsonElement body = null;

    public MockRequest(MockRequestServices services) {
        this.services = services;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // configuration
    // ----------------------------------------------------------------------------------------------------------------

    public void setMethod(String method) {
        this.method = method;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = Map.copyOf(headers);
    }

    public void setPathArguments(List<MockPathArgument> pathArguments) {
        List<PathArgument> result = new ArrayList<>();
        for (int i = 0; i < pathArguments.size(); i++) {
            MockPathArgument argument = pathArguments.get(i);
            String name = argument.name() == null ? "arg" + i : argument.name();
            result.add(new PathArgument(name, argument.value(), services.fromStringParserRegistry()));
        }
        this.pathArguments = List.copyOf(result);
    }

    public void setQuerystring(Map<String, String> querystring) {
        this.querystring = Map.copyOf(querystring);
    }

    public void setBody(JsonElement body) {
        this.body = body;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // interface
    // ----------------------------------------------------------------------------------------------------------------

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public List<PathArgument> getPathArguments() {
        return pathArguments;
    }

    @Override
    public Object parseQuerystring(Type type) throws QuerystringParsingException {
        Objects.requireNonNull(type, "type");

        try {
            var parser = services.querystringParserRegistry().get(type);
            return NullReturnCheckingCalls.parse(parser, querystring, type);
        } catch (NotRegisteredException e) {
            throw new QuerystringParsingException(Map.of("(root)", e.getMessage()));
        }
    }

    public Object parseBody(Type type) {
        Objects.requireNonNull(type, "type");

        try {
            return services.jsonEngine().deserialize(body, type);
        } catch (JsonDeserializationException e) {
            throw new FinishRequestException(StandardErrorResponse.requestBodyValidationFailed(e));
        }
    }

}
