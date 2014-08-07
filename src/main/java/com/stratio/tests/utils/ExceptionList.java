package com.stratio.tests.utils;

import java.util.ArrayList;
import java.util.List;

public class ExceptionList {

	private static ExceptionList instance = new ExceptionList();
	private final List<Exception> exceptions = new ArrayList<Exception>();

	private ExceptionList() {
	}

	public static ExceptionList getInstance() {
		return instance;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}
}
