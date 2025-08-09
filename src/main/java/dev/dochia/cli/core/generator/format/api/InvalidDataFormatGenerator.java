package dev.dochia.cli.core.generator.format.api;

/**
 * Provide implementation for different formats
 */
public interface InvalidDataFormatGenerator extends DataFormatGenerator {

    /**
     * This method will provide values which seem almost valid for the given format. For example: for an email field something like 'dochia@dochia.'
     *
     * @return an almost valid string based on the associated format
     */
    String getAlmostValidValue();

    /**
     * This method will provide values which are obviously not valid for the given format. For example: for an email field something like 'dochia'.
     *
     * @return a wrong value based on the associated format
     */
    String getTotallyWrongValue();
}
