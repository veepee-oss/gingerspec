package com.stratio.cucumber.converter;

import cucumber.api.Transformer;

public class NullableStringConverter extends Transformer<String> {
    public static final int DEFAULT_RADIX = 16;
    @Override
    public String transform(String input) {

        if ("//NONE//".equals(input)) {
            return null;
        } else if ("//NULL//".equals(input)) {
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