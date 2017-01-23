package com.stratio.qa.specs;

import com.stratio.qa.utils.ThreadProperty;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by carlosgarcia on 29/07/16.
 */
public class WhenGTest {

    @Test
    public void testSortJSONElementsAscending() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String ascendingFile = "indicesJSONAscending.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));
        String jsonStringAscending = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(ascendingFile).getFile())));

        CommonG commong = new CommonG();
        WhenGSpec wheng = new WhenGSpec(commong);

        ThreadProperty.set(envVar, jsonString);

        try {
            wheng.sortElements(envVar, "alphabetical", "ascending");
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        String value = ThreadProperty.get(envVar);

        assertThat(value).as("Not correctly ordered").isEqualTo(jsonStringAscending);
    }

    @Test
    public void testSortJSONElementsDescending() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String descendingFile = "indicesJSONDescending.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));
        String jsonStringDescending = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(descendingFile).getFile())));

        CommonG commong = new CommonG();
        WhenGSpec wheng = new WhenGSpec(commong);

        ThreadProperty.set(envVar, jsonString);

        try {
            wheng.sortElements(envVar, "alphabetical", "descending");
        } catch (Exception e) {
            fail("Error parsing JSON String");
        }

        String value = ThreadProperty.get(envVar);

        assertThat(value).as("Not correctly ordered").isEqualTo(jsonStringDescending);
    }

}
