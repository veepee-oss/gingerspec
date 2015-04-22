package com.stratio.cucumber.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import org.testng.annotations.Test;

public class ArrayListConverterTest {
    private ArrayListConverter converter = new ArrayListConverter();

    @Test
    public void test() {
        assertThat("Empty input converter", converter.transform(""), hasSize(1));
    }

    @Test
    public void test_1() {
        assertThat("Single string input converter", converter.transform("foo"), hasSize(1));
    }

    @Test
    public void test_2() {
        assertThat("Single string input converter", converter.transform("foo"), hasItem("foo"));
    }

    @Test
    public void test_3() {
        assertThat("Complex string input converter", converter.transform("foo,bar"), hasSize(2));
    }

    @Test
    public void test_4() {
        assertThat("Single string input converter", converter.transform("foo , bar"),
                allOf(hasItem("foo"), hasItem("bar")));
    }

    @Test
    public void test_5() {
        assertThat("Single string input converter", converter.transform("foo , , bar"),
                allOf(hasItem("foo"), hasItem(" "), hasItem("bar")));
    }
}
