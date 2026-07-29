package com.edison.scanner.exception;

/**
 * Exception thrown when communication with Binance fails.
 */
public class BinanceException extends RuntimeException {

    /**
     * Creates a Binance exception.
     *
     * @param message error message
     */
    public BinanceException(String message) {
        super(message);
    }

    /**
     * Creates a Binance exception.
     *
     * @param message error message
     * @param cause root cause
     */
    public BinanceException(String message, Throwable cause) {
        super(message, cause);
    }

}