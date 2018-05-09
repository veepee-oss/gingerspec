package com.privalia.qa.ATests;

import com.privalia.qa.cucumber.testng.CucumberRunner;
import com.privalia.qa.utils.BaseGTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;

@CucumberOptions(format = "json:target/FileParserStepsIT.json", features = {
        "src/test/resources/features/fileParserSteps.feature"},
        glue = "classpath:com/privalia/qa/specs/*")
public class FileParserStepsIT extends BaseGTest {

    @Test
    public void FileParserStepsIT() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
