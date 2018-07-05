package com.shazam.fork;

public class ForkException extends RuntimeException {
    public ForkException() {
    }

    public ForkException(String message) {
        super(message);
    }

    public ForkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForkException(Throwable cause) {
        super(cause);
    }
}
