package com.stratio.cucumber.converter;

import cucumber.api.Transformer;

public class StreamNameConverter extends Transformer<String> {

	@Override
	public String transform(String input) {

		if (input.equals("//NULL//")) {
			return null;
		} else {
			return input;
		}

	}
}