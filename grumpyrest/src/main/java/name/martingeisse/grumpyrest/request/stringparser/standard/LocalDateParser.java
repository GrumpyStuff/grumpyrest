/*
 * Copyright (c) 2023 Martin Geisse
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package name.martingeisse.grumpyrest.request.stringparser.standard;

import name.martingeisse.grumpyrest.request.stringparser.FromStringParser;
import name.martingeisse.grumpyrest.request.stringparser.FromStringParserException;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Objects;

/**
 * A parser for {@link LocalDate}.
 */
public final class LocalDateParser implements FromStringParser {

    /**
     * Constructor
     */
    public LocalDateParser() {
        // needed to silence Javadoc error because the implicit constructor doesn't have a doc comment
    }

    @Override
    public boolean supportsType(Type type) {
        Objects.requireNonNull(type, "type");

        return type.equals(LocalDate.class);
    }

    @Override
    public Object parseFromString(String s, Type type) throws FromStringParserException {
        Objects.requireNonNull(s, "s");
        Objects.requireNonNull(type, "type");

        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new FromStringParserException(e.getMessage());
        }
    }

}
