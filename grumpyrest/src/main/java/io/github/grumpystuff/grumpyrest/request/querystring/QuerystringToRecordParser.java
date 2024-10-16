/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package io.github.grumpystuff.grumpyrest.request.querystring;

import io.github.grumpystuff.grumpyjson.builtin.record.RecordInfo;
import io.github.grumpystuff.grumpyrest.ExceptionMessages;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParser;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserException;
import io.github.grumpystuff.grumpyrest.request.stringparser.FromStringParserRegistry;
import io.github.grumpystuff.grumpyrest.util.NullReturnCheckingCalls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * This class implements an auto-generated record parser.
 */
public final class QuerystringToRecordParser implements QuerystringParser {

    private final RecordInfo recordInfo;
    private final FromStringParserRegistry fromStringParserRegistry;

    QuerystringToRecordParser(Class<?> rawRecordClass, FromStringParserRegistry fromStringParserRegistry) {
        Objects.requireNonNull(rawRecordClass, "rawRecordClass");
        Objects.requireNonNull(fromStringParserRegistry, "fromStringParserRegistry");

        this.recordInfo = new RecordInfo(rawRecordClass);
        this.fromStringParserRegistry = fromStringParserRegistry;
    }

    @Override
    public boolean supportsType(Type type) {
        Objects.requireNonNull(type, "type");

        if (type instanceof Class<?>) {
            return type.equals(recordInfo.getRecordClass());
        } else if (type instanceof ParameterizedType p && p.getRawType() instanceof Class<?>) {
            return p.getRawType().equals(recordInfo.getRecordClass());
        } else {
            return false;
        }
    }

    @Override
    public Object parse(Map<String, String> querystring, Type recordType) throws QuerystringParsingException {
        Objects.requireNonNull(querystring, "querystring");
        Objects.requireNonNull(recordType, "recordType");

        List<RecordInfo.ComponentInfo> componentInfos = recordInfo.getComponentInfos();
        int numberOfPresentParameters = 0;
        Object[] fieldValues = new Object[componentInfos.size()];
        Map<String, String> fieldErrors = new HashMap<>();

        for (int i = 0; i < componentInfos.size(); i++) {
            RecordInfo.ComponentInfo componentInfo = componentInfos.get(i);
            String name = componentInfo.getName();
            String value = querystring.get(name);
            if (value != null) {
                numberOfPresentParameters++;
            }
            try {
                Type concreteFieldType = componentInfo.getConcreteType(recordType);
                FromStringParser parser = fromStringParserRegistry.get(concreteFieldType);
                if (value == null) {
                    fieldValues[i] = NullReturnCheckingCalls.parseFromAbsentString(parser, concreteFieldType);
                } else {
                    fieldValues[i] = NullReturnCheckingCalls.parseFromString(parser, value, concreteFieldType);
                }
            } catch (FromStringParserException e) {
                fieldErrors.put(name, e.getMessage());
            } catch (Exception e) {
                fieldErrors.put(name, "parse error");
            }
        }

        if (numberOfPresentParameters != querystring.size()) {
            // this is more expensive, so only do this if there is really an error
            Set<String> propertyNames = new HashSet<>(querystring.keySet());
            for (RecordInfo.ComponentInfo componentInfo : componentInfos) {
                propertyNames.remove(componentInfo.getName());
            }
            for (String unexpectedProperty : propertyNames) {
                fieldErrors.put(unexpectedProperty, ExceptionMessages.UNEXPECTED_PARAMETER);
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new QuerystringParsingException(Map.copyOf(fieldErrors));
        }
        try {
            return recordInfo.invokeConstructor(fieldValues);
        } catch (InvocationTargetException e) {
            // Since records are considered data containers, we expect exceptions from a record constructor to be
            // related to the record arguments, which we know. So returning the exception message in the response
            // should not leak any sensitive information. This allows error messages related to the *combination*
            // of multiple fields to be visible in the response without writing any custom code.
            throw new QuerystringParsingException(Map.of("(root)", e.getTargetException().getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
