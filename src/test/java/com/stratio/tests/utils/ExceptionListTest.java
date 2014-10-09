package com.stratio.tests.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import org.testng.annotations.Test;

public class ExceptionListTest {

    @Test
    public void test() {
        assertThat("Non empty Exception list on boot", ExceptionList.INSTANCE.getExceptions(), hasSize(0));
    }

}
