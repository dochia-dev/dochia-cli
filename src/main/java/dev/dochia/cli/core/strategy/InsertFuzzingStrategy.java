package dev.dochia.cli.core.strategy;

import dev.dochia.cli.core.util.CommonUtils;

/**
 * Fuzzing strategy that inserts fuzzed values into valid data.
 */
public final class InsertFuzzingStrategy extends FuzzingStrategy {
    @Override
    public Object process(Object value) {
        return CommonUtils.insertInTheMiddle(String.valueOf(value), String.valueOf(data), true);
    }

    @Override
    public String name() {
        return "INSERT";
    }
}
