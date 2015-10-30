package com.stratio.exceptions;

/**
 * Created by mpenate on 28/10/15.
 */
public class IncludeException extends Exception{
    public IncludeException(String message) {
        super(message);
    }

    public IncludeException(){
        super();
    }

    public IncludeException(String message, Throwable cause){
        super(message, cause);
    }

    public IncludeException(Throwable cause){
        super(cause);
    }

}
