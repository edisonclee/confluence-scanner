package com.edison.scanner.exceptions;

/**
 * Exception thrown when communication with an exchange fails.
 */
public final class ExchangeException
        extends RuntimeException {

    /**
	     * 
	     */
	    private static final long serialVersionUID = 1L;

	/**
     * Creates a new exchange exception.
     *
     * @param message error message
     */
    public ExchangeException(
            String message) {

        super(message);

    }

    /**
     * Creates a new exchange exception.
     *
     * @param message error message
     * @param cause root cause
     */
    public ExchangeException(
            String message,
            Throwable cause) {

        super(message, cause);

    }

}