package com.stratio.qa.aspects;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RunOnEnvTagAspectTest {

    public RunOnTagAspect runontag = new RunOnTagAspect();

    @Test
    public void testGetParams() throws Exception {
        String[] expectedResponse = {"HELLO", "BYE"};
        assertThat(expectedResponse).as("Params are correctly obtained").isEqualTo(runontag.getParams("@runOnEnv(HELLO,BYE)"));
    }

    @Test
    public void testCheckParams() throws Exception {
        System.setProperty("HELLO_OK","OK");
        assertThat(true).as("Params are correctly checked").isEqualTo(runontag.checkParams(runontag.getParams("@runOnEnv(HELLO_OK)")));
    }

    @Test
    public void testCheckParams_2() throws Exception {
        System.setProperty("HELLO_KO","KO");
        assertThat(false).as("Params are correctly checked 2").isEqualTo(runontag.checkParams(runontag.getParams("@runOnEnv(HELLO_KO,BYE_KO)")));
    }
}
