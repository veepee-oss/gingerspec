package com.stratio.tests.utils;

import java.util.ArrayList;
import java.util.List;
/**
 * Exception list class(Singleton).
 * @author Hugo Dominguez
 * @author Javier Delgado.
 *
 */
public enum ExceptionList {
    INSTANCE;

    private final List<Exception> exceptions = new ArrayList<Exception>();

    public List<Exception> getExceptions() {
        return exceptions;
    }

}