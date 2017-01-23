package com.stratio.qa.cucumber.converter;

import cucumber.api.Transformer;

public class NullableIntegerConverter extends Transformer<Integer> {

    @Override
    public Integer transform(String input) {

        if ("//NULL//".equals(input) || "".equals(input)) {
            return null;
        } else {
            return Integer.parseInt(input);
        }

    }
}