/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package name.martingeisse.grumpyrest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import name.martingeisse.grumpyjson.ExceptionMessages;
import name.martingeisse.grumpyjson.JsonValidationException;
import name.martingeisse.grumpyrest.finish.FinishRequestException;
import name.martingeisse.grumpyrest.path.PathSegment;
import name.martingeisse.grumpyrest.path.PathUtil;
import name.martingeisse.grumpyrest.path.VariablePathSegment;
import name.martingeisse.grumpyrest.querystring.QuerystringParsingException;
import name.martingeisse.grumpyrest.responder.standard.StandardErrorResponder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

public final class RequestCycle {

    private final RestApi api;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ImmutableList<String> pathSegments;
    private Route matchedRoute;
    private ImmutableList<PathArgument> pathArguments;

    public RequestCycle(RestApi api, HttpServletRequest request, HttpServletResponse response) {
        this.api = api;
        this.request = request;
        this.response = response;

        String pathText = request.getServletPath();
        if (pathText == null) {
            this.pathSegments = ImmutableList.of();
        } else {
            this.pathSegments = ImmutableList.copyOf(PathUtil.splitIntoSegments(pathText));
        }
    }

    public RestApi getApi() {
        return api;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public ImmutableList<String> getPathSegments() {
        return pathSegments;
    }

    public Route getMatchedRoute() {
        return matchedRoute;
    }

    void setMatchedRoute(Route matchedRoute) {
        Objects.requireNonNull(matchedRoute, "matchedRoute");

        ImmutableList<PathSegment> matchedRouteSegments = matchedRoute.path().segments();
        if (matchedRouteSegments.size() != pathSegments.size()) {
            throw new IllegalArgumentException("matched route has different number of segments than the path of this request cycle");
        }

        List<PathArgument> newPathArguments = new ArrayList<>();
        for (int i = 0; i < pathSegments.size(); i++) {
            PathSegment routeSegment = matchedRouteSegments.get(i);
            if (routeSegment instanceof VariablePathSegment variable) {
                newPathArguments.add(new PathArgument(this, variable.getVariableName(), pathSegments.get(i)));
            }
        }

        this.matchedRoute = matchedRoute;
        this.pathArguments = ImmutableList.copyOf(newPathArguments);
    }

    public <T> T parseBody(Class<T> clazz) throws JsonValidationException {
        return api.getJsonEngine().parse(prepareParse(), clazz);
    }

    public <T> T parseBody(TypeToken<T> typeToken) throws JsonValidationException {
        return api.getJsonEngine().parse(prepareParse(), typeToken);
    }

    public Object parseBody(Type type) throws JsonValidationException {
        return api.getJsonEngine().parse(prepareParse(), type);
    }

    private InputStream prepareParse() {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.equals("application/json")) {
            throw new FinishRequestException(StandardErrorResponder.JSON_EXPECTED);
        }
        try {
            return request.getInputStream();
        } catch (IOException e) {
            throw new FinishRequestException(StandardErrorResponder.IO_ERROR);
        }
    }

    public ImmutableList<PathArgument> getPathArguments() {
        if (pathArguments == null) {
            throw new IllegalStateException("no route matched yet");
        }
        return pathArguments;
    }

    public <T> T parseQuerystring(Class<T> clazz) throws QuerystringParsingException {
        return clazz.cast(parseQuerystring((Type)clazz));
    }

    public <T> T parseQuerystring(TypeToken<T> typeToken) throws QuerystringParsingException {
        //noinspection unchecked
        return (T)parseQuerystring(typeToken.getType());
    }

    public Object parseQuerystring(Type type) throws QuerystringParsingException {
        Map<String, String[]> querystringMulti = request.getParameterMap();
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
            result = api.getQuerystringParserRegistry().getParser(type).parse(querystringSingle, type);
            if (result == null) {
                throw new QuerystringParsingException(ImmutableMap.of("<root>", "querystring parser returned null"));
            }
        } catch (QuerystringParsingException e) {
            originalException = e;
            // duplicate-parameter errors take precedence here
            for (Map.Entry<String, String> entry : e.getFieldErrors().entrySet()) {
                errorMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
        if (!errorMap.isEmpty()) {
            throw new QuerystringParsingException(ImmutableMap.copyOf(errorMap));
        }
        if (result == null) {
            // this can only happen if the originalException did not contain any errors
            throw originalException;
        }
        return result;
    }

}
