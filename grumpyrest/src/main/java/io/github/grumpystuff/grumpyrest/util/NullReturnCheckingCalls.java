package io.github.grumpystuff.grumpyrest.util;

import io.github.grumpystuff.grumpyrest.request.querystring.QuerystringParser;
import io.github.grumpystuff.grumpyrest.request.querystring.QuerystringParsingException;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * NOT PUBLIC API
 */
public class NullReturnCheckingCalls {

    // prevent instantiation
    private NullReturnCheckingCalls() {
    }

    /**
     * NOT PUBLIC API
     *
     * @param querystring ...
     * @param type ...
     * @return ...
     * @throws QuerystringParsingException ...
     */
    public static Object parse(QuerystringParser querystringParser, Map<String, String> querystring, Type type) throws QuerystringParsingException {
        return Objects.requireNonNull(querystringParser.parse(querystring, type), "querystring parser returned null");
    }

    /**
     * NOT PUBLIC API
     *
     * @param fromStringParser ...
     * @param s ...
     * @param type ...
     * @return ...
     * @throws FromStringParserException ...
     */
    public static Object parseFromString(FromStringParser fromStringParser, String s, Type type) throws FromStringParserException {
        return Objects.requireNonNull(fromStringParser.parseFromString(s, type), "from-string parser returned null");
    }

    /**
     * NOT PUBLIC API
     *
     * @param fromStringParser ...
     * @param type ...
     * @return ...
     * @throws FromStringParserException ...
     */
    public static Object parseFromAbsentString(FromStringParser fromStringParser, Type type) throws FromStringParserException {
        return Objects.requireNonNull(fromStringParser.parseFromAbsentString(type), "from-string parser returned null");
    }

}
