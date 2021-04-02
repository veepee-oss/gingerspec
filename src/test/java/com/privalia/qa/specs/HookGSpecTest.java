package com.privalia.qa.specs;

import org.openqa.selenium.MutableCapabilities;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.NoSuchFileException;

public class HookGSpecTest {

    final  HookGSpec hgs  = new HookGSpec(null);

    @Test
    public void testWrongFilePath() throws IOException {

        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "chrome");
        Assert.expectThrows(NoSuchFileException.class, () -> hgs.addCapabilitiesFromFile("hola", capabilities));

    }

    @Test
    public void testMerge() throws IOException {

        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", "chrome");
        hgs.addCapabilitiesFromFile("src/test/resources/capabilities.json", capabilities);
        Assert.assertEquals(capabilities.getCapability("browserName"),"chrome");
        Assert.assertNotNull(capabilities.getPlatform());
        Assert.assertNotNull(capabilities.getCapability("chromeOptions"));
        Assert.assertEquals(capabilities.getPlatform().name(), "ANY");
    }
}