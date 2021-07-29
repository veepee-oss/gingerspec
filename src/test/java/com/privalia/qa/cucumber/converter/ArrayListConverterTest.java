/*
 * Copyright (c) 2021, Veepee
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose
 * with or without fee is hereby  granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 *
 * THE SOFTWARE  IS PROVIDED "AS IS"  AND THE AUTHOR DISCLAIMS  ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS.  IN NO  EVENT  SHALL THE  AUTHOR  BE LIABLE  FOR  ANY SPECIAL,  DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
 * OF USE, DATA  OR PROFITS, WHETHER IN AN ACTION OF  CONTRACT, NEGLIGENCE OR OTHER
 * TORTIOUS ACTION, ARISING OUT OF OR  IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
*/

package com.privalia.qa.cucumber.converter;


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
