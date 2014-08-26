package com.stratio.cucumber.converter;

import cucumber.api.Transformer;

public class NullableStringConverter extends Transformer<String> {

	@Override
	public String transform(String input) {

		if ("//NULL//".equals(input)) {
			return null;
		} else if (input.startsWith("0x")) {

			int cInt = Integer.parseInt(input.substring(2), 16);
			char[] cArr = Character.toChars(cInt);			
			return String.valueOf(cArr);
		} else {
			return input;
		}

	}
}