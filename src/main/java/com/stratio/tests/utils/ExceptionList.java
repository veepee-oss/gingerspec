package com.stratio.tests.utils;

import java.util.ArrayList;
import java.util.List;

public enum ExceptionList {
    INSTANCE;

    private final List<Exception> exceptions = new ArrayList<Exception>();

    public List<Exception> getExceptions() {
        return exceptions;
    }

}