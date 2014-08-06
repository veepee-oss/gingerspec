package com.stratio.specs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stratio.tests.utils.ThreadProperty;

public class CommonG {

	private final Logger logger = LoggerFactory.getLogger(ThreadProperty
			.get("class"));
	private List<Exception> exceptions = new ArrayList<Exception>();

	public Logger getLogger() {
		return this.logger;
	}

	public List<Exception> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<Exception> exceptions) {
		this.exceptions = exceptions;
	}
}
