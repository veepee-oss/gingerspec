package com.stratio.cucumber.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import org.testng.annotations.Test;

public class ArrayListConverterTest {

    @Test
    public void test() {
        ArrayListConverter converter = new ArrayListConverter();
        assertThat("Empty input converter", converter.transform(""), hasSize(1));
        assertThat("Single string input converter", converter.transform("foo"), hasSize(1));
        assertThat("Single string input converter", converter.transform("foo"), hasItem("foo"));
        assertThat("Complex string input converter", converter.transform("foo,bar"), hasSize(2));
        assertThat("Single string input converter", converter.transform("foo , bar"),
                allOf(hasItem("foo "), hasItem(" bar")));
    }

}
