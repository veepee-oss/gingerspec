package com.stratio.exceptions;

public class IncludeException extends Exception {

    public IncludeException(String message) {
        super(message);
    }

    public IncludeException() {
        super();
    }

    public IncludeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncludeException(Throwable cause) {
        super(cause);
    }

}