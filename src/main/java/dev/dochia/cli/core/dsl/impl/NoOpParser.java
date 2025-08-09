package dev.dochia.cli.core.dsl.impl;

import dev.dochia.cli.core.dsl.api.Parser;
import java.util.Map;

/**
 * No operation parser. Returns the same input expression.
 */
public class NoOpParser implements Parser {
    @Override
    public String parse(String expression, Map<String, String> context) {
        return expression;
    }
}
