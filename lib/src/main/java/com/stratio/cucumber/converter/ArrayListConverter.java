package com.stratio.cucumber.converter;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.Transformer;

/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 *
 */
public class ArrayListConverter extends Transformer<List<String>> {

    @Override
    public List<String> transform(String input) {

        List<String> response = new ArrayList<String>();
        String[] aInput = input.split(",");
        for (String content : aInput) {
            if (content.trim().equals("")) {
                response.add(content);
            } else {
                response.add(content.trim());
            }
        }

        return response;
    }
}
