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
package com.privalia.qa.specs;

import com.privalia.qa.utils.ThreadProperty;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GivenGTest {

    @Test
    public void testSaveElementFromVariable() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();
        RestSpec giveng = new RestSpec(commong);

        try {
            giveng.saveElementEnvironment(null, jsonString.concat(".$.[0]"), envVar);
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        assertThat(ThreadProperty.get(envVar)).as("Not correctly ordered").isEqualTo("dummylogs-2016-07-26");
    }
}
