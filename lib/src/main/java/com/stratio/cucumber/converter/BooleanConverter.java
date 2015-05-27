package com.stratio.cucumber.converter;

import cucumber.api.Transformer;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class BooleanConverter extends Transformer<Boolean> {

    @Override
    public Boolean transform(String input) {
        return "".equals(input);

    }
}