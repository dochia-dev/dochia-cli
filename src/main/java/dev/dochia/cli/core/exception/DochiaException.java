package dev.dochia.cli.core.exception;

/**
 * A custom runtime exception for dochia-related exceptions, extending {@link RuntimeException}.
 */
public class DochiaException extends RuntimeException {

    /**
     * Constructs a new {@code DochiaException} with the specified cause.
     *
     * @param e the cause of the exception
     */
    public DochiaException(Exception e) {
        super(e);
    }

    /**
     * Constructs a new {@code DochiaException} with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param e       the cause of the exception
     */
    public DochiaException(String message, Exception e) {
        super(message, e);
    }
}
