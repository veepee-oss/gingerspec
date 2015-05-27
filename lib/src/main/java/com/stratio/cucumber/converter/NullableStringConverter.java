package com.stratio.cucumber.converter;

import cucumber.api.Transformer;
/**
 * @author Javier Delgado
 * @author Hugo Dominguez
 */
public class NullableStringConverter extends Transformer<String> {
    public static final int DEFAULT_RADIX = 16;
    @Override
    public String transform(String input) {

        if ("//NULL//".equals(input)) {
            return null;
        } else if (input.startsWith("0x")) {

            int cInt = Integer.parseInt(input.substring(2), DEFAULT_RADIX);
            char[] cArr = Character.toChars(cInt);
            return String.valueOf(cArr);
        } else {
            return input;
        }

    }
}