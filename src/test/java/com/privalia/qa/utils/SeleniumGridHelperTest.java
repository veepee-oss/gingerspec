package com.privalia.qa.utils;

import org.junit.Assert;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class SeleniumGridHelperTest {

    @Test(enabled = false)
    public void getAllNodes() {
        List<String> availableNodes = new SeleniumGridHelper()
                .connectToGrid("localhost:4444")
                .getAllNodes();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isGreaterThan(0);
    }

    @Test(enabled = false)
    public void getAllAvailableNodes() {
        List<String> availableNodes = new SeleniumGridHelper()
                .connectToGrid("localhost:4444")
                .getAllAvailableNodes();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isGreaterThan(0);
    }

    @Test
    public void testFromFile() throws IOException {
        String content = new String(Files.readAllBytes( Paths.get("src/test/resources/grid.html")));

        List<String> availableNodes = new SeleniumGridHelper()
                .setPageSource(content)
                .getAllAvailableNodes();

        Assert.assertNotNull(availableNodes);
        assertThat(availableNodes.size()).isEqualTo(4);
    }

    @Test
    public void getNodesByFilter() {
        final List<String> availableNodes = new LinkedList<>();
        availableNodes.add("{server:CONFIG_UUID=e1338830-5d89-460b-acee-c5b465ee4c6c, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=firefox, maxInstances=1, platformName=LINUX, screenResolution=400x400, resolution=1920x1080, version=82.0.3, platform=LINUX}");
        availableNodes.add("{server:CONFIG_UUID=2f5d203a-8fdd-4147-b29e-4c877cb75f69, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=chrome, maxInstances=1, platformName=LINUX, screenResolution=1920x1080, resolution=1920x1080, version=87.0.4280.66, platform=LINUX}");
        availableNodes.add("{server:CONFIG_UUID=be713c94-8f8b-4bc6-b706-5f129a940ea8, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=firefox, maxInstances=1, platformName=WINDOWS, screenResolution=400x400, resolution=1920x1080, version=82.0.3, platform=WINDOWS}");
        availableNodes.add("{server:CONFIG_UUID=ce5b55af-e4e4-440e-90bc-4bfcace17d2c, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=chrome, maxInstances=1, platformName=WINDOWS, screenResolution=1920x1080, resolution=1920x1080, version=87.0.4280.66, platform=WINDOWS}");
        availableNodes.add("{server:CONFIG_UUID=ce5b55af-e4e4-440e-90bc-4bfcace17d2c, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=chrome, maxInstances=1, platformName=Android, screenResolution=1920x1080, resolution=1920x1080, version=87.0.4280.66, platform=Android}");
        availableNodes.add("{server:CONFIG_UUID=ce5b55af-e4e4-440e-90bc-4bfcace17d2c, seleniumProtocol=WebDriver, acceptSslCerts=true, screen-resolution=1920x1080, tz=Europe/Berlin, browserName=chrome, maxInstances=1, platformName=iOS, screenResolution=1920x1080, resolution=1920x1080, version=87.0.4280.66, platform=iOS}");

        Map<String, String> filter = new HashMap<String, String>();
        filter.put("platformName", "(Android|iOS)");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("platformName", "Android");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("platformName", "iOS");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "chrome");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(4);

        filter.clear();
        filter.put("browserName", "(chrome|firefox)");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(6);

        filter.clear();
        filter.put("platform", "(LINUX|WINDOWS)");
        filter.put("screenResolution", "400x400");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("browserName", "firefox");
        filter.put("platform", "WINDOWS");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "chrome");
        filter.put("platform", "LINUX");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(1);

        filter.clear();
        filter.put("browserName", "(chrome|firefox)");
        filter.put("platform", "LINUX");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(2);

        filter.clear();
        filter.put("platform", "(iOS|Android|WINDOWS)");
        assertThat(new SeleniumGridHelper().filterNodes(availableNodes, filter).size()).isEqualTo(4);

    }
}