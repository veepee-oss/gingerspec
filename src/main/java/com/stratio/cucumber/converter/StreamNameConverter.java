package com.stratio.cucumber.converter;

import cucumber.api.Transformer;

public class StreamNameConverter extends Transformer<String> {

	@Override
	public String transform(String input) {

		if (input.equals("//NULL//")) {
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