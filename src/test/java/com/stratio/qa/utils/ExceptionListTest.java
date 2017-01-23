package com.stratio.qa.utils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionListTest {

    @Test
    public void test() {
        assertThat(ExceptionList.INSTANCE.getExceptions()).as("Non empty Exception list on boot").hasSize(0);
    }
}
