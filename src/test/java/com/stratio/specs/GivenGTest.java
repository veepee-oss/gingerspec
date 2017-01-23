package com.stratio.specs;

import com.stratio.tests.utils.ThreadProperty;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by carlosgarcia on 29/07/16.
 */
public class GivenGTest {

    @Test
    public void testSaveElementFromVariable() throws Exception {
        ThreadProperty.set("class", this.getClass().getCanonicalName());

        String baseData = "indicesJSON.conf";
        String envVar = "envVar";

        String jsonString = new String(Files.readAllBytes(
                Paths.get(getClass().getClassLoader().getResource(baseData).getFile())));

        CommonG commong = new CommonG();
        GivenGSpec giveng = new GivenGSpec(commong);

        try{
            giveng.saveElementEnvironment(null,null,jsonString.concat(".$.[0]"),envVar);
        }catch(Exception e){
            fail("Error parsing JSON String");
        }

        assertThat(ThreadProperty.get(envVar)).as("Not correctly ordered").isEqualTo("stratiopaaslogs-2016-07-26");
    }

}
