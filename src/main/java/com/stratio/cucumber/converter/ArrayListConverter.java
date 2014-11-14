package com.stratio.cucumber.converter;

import java.util.ArrayList;

import cucumber.api.Transformer;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class ArrayListConverter extends Transformer<ArrayList<String>> {

    @Override
    public ArrayList<String> transform(String input) {

        ArrayList<String> response = new ArrayList<String>();
        String[] aInput = input.split(",");
        for (String content : aInput) {
            response.add(content);
        }

        return response;
    }
}
