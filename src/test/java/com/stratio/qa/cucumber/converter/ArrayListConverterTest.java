package com.stratio.qa.cucumber.converter;


import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayListConverterTest {
    private ArrayListConverter converter = new ArrayListConverter();

    @Test
    public void test() {
        assertThat(converter.transform("")).as("Empty input converter").hasSize(1);
    }

    @Test
    public void test_1() {
        assertThat(converter.transform("foo")).as("Single string input converter").hasSize(1);
    }

    @Test
    public void test_2() {
        assertThat(converter.transform("foo")).as("Single string input converter").contains("foo");
    }

    @Test
    public void test_3() {
        assertThat(converter.transform("foo,bar")).as("Complex string input converter").hasSize(2);
    }

    @Test
    public void test_4() {
        assertThat(converter.transform("foo , bar")).as("Single string input converter").contains("foo", "bar");
    }

    @Test
    public void test_5() {
        assertThat(converter.transform("foo , , bar")).as("Single string input converter").contains("foo", " ", "bar");
    }

    @Test
    public void test_6() {
        assertThat(converter.transform("foo ,   , bar")).as("Single string input converter").contains("foo", "   ", "bar");
    }

}
