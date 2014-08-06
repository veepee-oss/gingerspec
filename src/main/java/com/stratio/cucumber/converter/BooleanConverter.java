package com.stratio.cucumber.converter;

import cucumber.api.Transformer;

public class BooleanConverter extends Transformer<Boolean> {

	@Override
	public Boolean transform(String input) {

		if (input.equals("")) {
			return true;
		} else {
			return false;
		}

	}
}