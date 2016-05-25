package com.stratio.exceptions;

public class NonReplaceableException extends Exception {

    public NonReplaceableException(String message) {
        super(message);
    }

    public NonReplaceableException() {
        super();
    }

    public NonReplaceableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonReplaceableException(Throwable cause) {
        super(cause);
    }

}