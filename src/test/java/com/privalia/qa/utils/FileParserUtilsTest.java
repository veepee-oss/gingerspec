package com.privalia.qa.utils;

import com.sonalake.utah.config.Config;
import com.sonalake.utah.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class FileParserUtilsTest {

    private FileParserUtils fileParserUtils = new FileParserUtils();
    private final Logger logger = LoggerFactory
            .getLogger(FileParserUtilsTest.class);
    private List<Map<String, String>> records;


    @Test(enabled = true)
    public void parseFileTest() throws IOException, JAXBException, URISyntaxException {

        this.getRecords();

        assertThat(this.records.size()).isEqualTo(3);
    }

    @Test(enabled = true)
    public void sumColumnTest() throws IOException, JAXBException, ParseException, URISyntaxException {

        this.getRecords();

        String sum = this.fileParserUtils.sumColumn(this.records,"localAS");
        assertThat(sum).isEqualTo("196653.0");

        String sum2 = this.fileParserUtils.sumColumn(this.records,"remoteAS");
        assertThat(sum2).isEqualTo("196656.0");

    }

    @Test(enabled = true)
    public void elementsWhereEqualTest() throws IOException, JAXBException, URISyntaxException {

        this.getRecords();

        assertThat(this.fileParserUtils.elementsWhereEqual(this.records,"localAS", "65551")).isEqualTo(3);
        assertThat(this.fileParserUtils.elementsWhereEqual(this.records,"remoteAS", "65551")).isEqualTo(1);
        assertThat(this.fileParserUtils.elementsWhereEqual(this.records,"status", "1")).isEqualTo(1);

    }

    @Test(enabled = true)
    public void getValueofColumnAtPositionTest() throws IOException, JAXBException, URISyntaxException {

        this.getRecords();

        assertThat(this.fileParserUtils.getValueofColumnAtPosition(this.records,"remoteAS", 1)).isEqualTo("65552");
        assertThat(this.fileParserUtils.getValueofColumnAtPosition(this.records,"uptime", 2)).isEqualTo("07:55:38");

    }

    @Test(enabled = true)
    public void getFirstRecordThatMatchesTest() throws IOException, JAXBException, URISyntaxException {

        this.getRecords();

        Map<String, String> result = this.fileParserUtils.getFirstRecordThatMatches(this.records,"remoteIp", "10.10.100.1");
        assertThat(result.get("status").trim()).isEqualTo("0");

    }

    @Test(enabled = true)
    public void filterRecordThatMatchesTest() throws IOException, JAXBException, URISyntaxException {

        this.getRecords();
        List<Map<String, String>> result = this.fileParserUtils.filterRecordThatMatches(this.records, "localAS", "65551", "equal");
        assertThat(result.size()).isEqualTo(3);

        result = this.fileParserUtils.filterRecordThatMatches(this.records, "status", "0", "not equal");
        assertThat(result.size()).isEqualTo(2);

        result = this.fileParserUtils.filterRecordThatMatches(this.records, "uptime", "10:", "contains");
        assertThat(result.size()).isEqualTo(2);
    }

    private void getRecords() throws IOException, JAXBException, URISyntaxException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream("files/f10_ip_bgp_summary_example.txt");
        Config config = new ConfigLoader().loadConfig(getClass().getClassLoader().getResource("files/f10_ip_bgp_summary_template.xml").toURI().toURL());
        Reader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        this.records = this.fileParserUtils.parseFile(config, in);
    }

}