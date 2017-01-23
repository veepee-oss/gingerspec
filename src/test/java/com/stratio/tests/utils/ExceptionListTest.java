package com.stratio.tests.utils;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ExceptionListTest {

    @Test
    public void test() {
        assertThat( ExceptionList.INSTANCE.getExceptions()).as("Non empty Exception list on boot").hasSize(0);
    }
}
