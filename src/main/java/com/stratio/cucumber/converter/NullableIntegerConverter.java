package com.stratio.cucumber.converter;

import cucumber.api.Transformer;
/**
 * 
 * @author Hugo Dominguez
 * @author Javier Delgado
 */
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