package com.stratio.qa.cucumber.converter;

import cucumber.api.Transformer;

public class BooleanConverter extends Transformer<Boolean> {

    @Override
    public Boolean transform(String input) {
        return "".equals(input);

    }
}