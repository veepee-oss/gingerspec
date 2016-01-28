package com.stratio.cucumber.aspects;


import com.stratio.exceptions.IncludeException;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.*;

/**
 * Created by mpenate on 24/11/15.
 */
public class IncludeTagAspectTest {
    public IncludeTagAspect inctag = new IncludeTagAspect();

    @Test
    public void testGetFeature() {
        assertThat("Test feature name is extracted correctly","test.feature", is(inctag.getFeatureName("@include(feature: test.feature,scenario: To copy)")));
    }

    @Test
    public void testGetScenario() {
        assertThat("Test scenario name is extracted correctly","To copy", is(inctag.getScenName("@include(feature: test.feature,scenario: To copy)")));
    }
    @Test
    public void testGetParams() {
        assertThat("Test that the number of keys and values are correctly calculated for params", 4, is(inctag.getParams("@include(feature: test.feature,scenario: To copy,params: [time1:9, time2:9])").length));
    }

    @Test
    public void testDoReplaceKeys () throws IncludeException {
        String keysNotReplaced = "Given that <time1> is not equal to <time2> into a step";
        String[] keys = {"<time1>","9","<time2>","8"};
        assertThat("Test that keys are correctly replaced at scenario outlines", "Given that 9 is not equal to 8 into a step", is(inctag.doReplaceKeys(keysNotReplaced,keys)));
    }

    @Test
    public void testCheckParams() throws IncludeException {
        String lineOfParams = "| hey | ho |";
        String[] keys = {"<time1>","9","<time2>","8"};

        String[] tonsOfKeys = {"<time1>","9","<time2>","23","33","32","10"};

        assertTrue(inctag.checkParams(lineOfParams, keys), "Test that include parameters match the number of them at the scenario outline included");
        assertThat("Test that include parameters match the number of them at the scenario outline included", false, is(inctag.checkParams(lineOfParams,tonsOfKeys)));

    }

}