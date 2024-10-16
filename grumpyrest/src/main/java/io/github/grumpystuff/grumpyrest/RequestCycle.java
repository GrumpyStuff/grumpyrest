/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyrest;

import io.github.grumpystuff.grumpyjson.TypeToken;
import io.github.grumpystuff.grumpyjson.deserialize.JsonDeserializationException;
import io.github.grumpystuff.grumpyjson.json_model.JsonElement;
import io.github.grumpystuff.grumpyjson.registry.NotRegisteredException;
import io.github.grumpystuff.grumpyjson.serialize.JsonSerializationException;
import io.github.grumpystuff.grumpyrest.request.PathArgument;
import io.github.grumpystuff.grumpyrest.request.Request;
import io.github.grumpystuff.grumpyrest.request.path.PathUtil;
import io.github.grumpystuff.grumpyrest.request.querystring.QuerystringParsingException;
import io.github.grumpystuff.grumpyrest.response.FinishRequestException;
import io.github.grumpystuff.grumpyrest.response.Response;
import io.github.grumpystuff.grumpyrest.response.ResponseTransmitter;
import io.github.grumpystuff.grumpyrest.servlet.RequestPathSourcingStrategy;
import io.github.grumpystuff.grumpyrest.util.NullReturnCheckingCalls;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.grumpystuff.grumpyrest.response.standard.StandardErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Holds the run-time state of processing a single request. Application code will normally not have to deal with a
 * request cycle directly, but rather implement a {@link SimpleHandler} that takes a {@link Request} and returns either
 * a {@link Response}, or a value that gets converted to such a response. Only if this scheme is not flexible enough
 * will the application implement a {@link ComplexHandler} and deal with the request cycle directly.
 */
public final class RequestCycle {

    private final RestApi api;
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;
    private final List<String> pathSegments;

    private RouteMatchResult routeMatchResult;

    private final Request highlevelRequest;
    private final ResponseTransmitter responseTransmitter;

    /**
     * NOT PUBLIC API
     *
     * @param api                         ...
     * @param servletRequest              ...
     * @param servletResponse             ...
     * @param requestPathSourcingStrategy ...
     */
    public RequestCycle(
        RestApi api,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse,
        RequestPathSourcingStrategy requestPathSourcingStrategy
    ) {
        Objects.requireNonNull(api, "api");
        Objects.requireNonNull(servletRequest, "servletRequest");
        Objects.requireNonNull(servletResponse, "servletResponse");
        Objects.requireNonNull(requestPathSourcingStrategy, "requestPathSourcingStrategy");

        this.api = api;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;

        String pathText = requestPathSourcingStrategy.getPath(servletRequest);
        if (pathText == null) {
            this.pathSegments = List.of();
        } else {
            this.pathSegments = List.of(PathUtil.splitIntoSegments(pathText));
        }

        this.highlevelRequest = new MyRequest();
        this.responseTransmitter = new MyResponseTransmitter();
    }

    /**
     * Getter method for the {@link RestApi} that handles the request
     *
     * @return the REST API implementation
     */
    public RestApi getApi() {
        return api;
    }

    /**
     * Getter method for the underlying servlet request
     *
     * @return the servlet request
     */
    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    /**
     * Getter method for the requested path, split into segments at slashes
     *
     * @return the path segments
     */
    public List<String> getPathSegments() {
        return pathSegments;
    }

    private RouteMatchResult needRouteMatchResult() {
        if (routeMatchResult == null) {
            throw new IllegalStateException("no route matched yet");
        }
        return routeMatchResult;
    }

    /**
     * Getter method for the route that matched this request. This method must not be called before route matching
     * is completed, otherwise it throws an exception.
     *
     * @return the route
     */
    public Route getMatchedRoute() {
        return needRouteMatchResult().route();
    }

    /**
     * Getter method for the path arguments that are bound to variables in the path of the matched route.
     * This method must not be called before route matching is completed, otherwise it throws an exception.
     * <p>
     * The returned list contains elements only for variables in the route's path, not for fixed path segments.
     *
     * @return the path arguments
     */
    public List<PathArgument> getPathArguments() {
        return needRouteMatchResult().pathArguments();
    }

    /**
     * The high-level {@link Request} object that makes all relevant properties of the HTTP request available. This is
     * the object that gets passed to a {@link SimpleHandler} as the only parameter.
     *
     * @return the request
     */
    public Request getHighlevelRequest() {
        return highlevelRequest;
    }

    /**
     * Returns an object that is used by {@link Response} implementations to transmit the response to the client. It is
     * an abstraction of the {@link HttpServletResponse} and contains methods to send headers as well as the response
     * body.
     * <p>
     * The returned object is called a response <i>transmitter</i>, even though it abstracts the servlet
     * <i>response</i>, because the latter suffers from bad naming: That object isn't really the response itself as
     * much as a mechanism to send a response to the client.
     *
     * @return the response transmitter
     */
    public ResponseTransmitter getResponseTransmitter() {
        return responseTransmitter;
    }

    void applyRouteMatchResult(RouteMatchResult matchResult) {
        Objects.requireNonNull(matchResult, "matchResult");

        this.routeMatchResult = matchResult;
    }

    private final class MyResponseTransmitter implements ResponseTransmitter {

        @Override
        public void setStatus(int status) {
            servletResponse.setStatus(status);
        }

        @Override
        public void setContentType(String contentType) {
            Objects.requireNonNull(contentType, "contentType");

            servletResponse.setContentType(contentType);
        }

        @Override
        public void addCustomHeader(String name, String value) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(value, "value");

            servletResponse.addHeader(name, value);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return servletResponse.getOutputStream();
        }

        @Override
        public void writeJson(Object value) throws JsonSerializationException, IOException {
            Objects.requireNonNull(value, "value");

            api.getJsonEngine().writeTo(value, servletResponse.getOutputStream());
        }

    }

    private final class MyRequest implements Request {

        private JsonElement preParsedBody;

        @Override
        public String getMethod() {
            return servletRequest.getMethod();
        }

        @Override
        public String getHeader(String name) {
            Objects.requireNonNull(name, "name");

            return servletRequest.getHeader(name);
        }

        public List<PathArgument> getPathArguments() {
            return RequestCycle.this.getPathArguments();
        }

        public Object parseQuerystring(Type type) throws QuerystringParsingException {
            Objects.requireNonNull(type, "type");

            Map<String, String[]> querystringMulti = servletRequest.getParameterMap();
            Map<String, String> querystringSingle = new HashMap<>();
            Map<String, String> errorMap = new HashMap<>();
            for (Map.Entry<String, String[]> entry : querystringMulti.entrySet()) {
                String[] values = entry.getValue();
                for (String value : values) {
                    if (querystringSingle.put(entry.getKey(), value) != null) {
                        errorMap.put(entry.getKey(), ExceptionMessages.DUPLICATE_PARAMETER);
                    }
                }
            }
            Object result = null;
            QuerystringParsingException originalException = null;
            try {
                var parser = api.getQuerystringParserRegistry().get(type);
                result = NullReturnCheckingCalls.parse(parser, querystringSingle, type);
                if (result == null) {
                    throw new QuerystringParsingException(Map.of("(root)", "querystring parser returned null"));
                }
            } catch (NotRegisteredException e) {
                throw new QuerystringParsingException(Map.of("(root)", e.getMessage()));
            } catch (QuerystringParsingException e) {
                originalException = e;
                // duplicate-parameter errors take precedence here
                for (Map.Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                    errorMap.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }
            if (!errorMap.isEmpty()) {
                throw new QuerystringParsingException(Map.copyOf(errorMap));
            }
            if (result == null) {
                // this can only happen if the originalException did not contain any errors
                throw originalException;
            }
            return result;
        }

        public Object parseBody(Type type) {
            Objects.requireNonNull(type, "type");

            try {
                return api.getJsonEngine().deserialize(prepareParse(), type);
            } catch (JsonDeserializationException e) {
                throw new FinishRequestException(StandardErrorResponse.requestBodyValidationFailed(e));
            }
        }

        private JsonElement prepareParse() {
            if (preParsedBody == null) {
                String contentType = servletRequest.getContentType();
                if (contentType == null || !contentType.equals("application/json")) {
                    throw new FinishRequestException(StandardErrorResponse.JSON_EXPECTED);
                }
                try {
                    preParsedBody = api.getJsonEngine().deserialize(servletRequest.getInputStream(), JsonElement.class);
                } catch (JsonDeserializationException e) {
                    throw new FinishRequestException(StandardErrorResponse.requestBodyValidationFailed(e));
                } catch (IOException e) {
                    throw new FinishRequestException(StandardErrorResponse.IO_ERROR);
                }
            }
            return preParsedBody;
        }

    }

}
