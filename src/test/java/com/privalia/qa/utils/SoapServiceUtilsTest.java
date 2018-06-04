package com.privalia.qa.utils;

import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class SoapServiceUtilsTest {

    SoapServiceUtils soap;

    private String addRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soap:Body>\n" +
            "    <Add xmlns=\"http://tempuri.org/\">\n" +
            "      <intA>int</intA>\n" +
            "      <intB>int</intB>\n" +
            "    </Add>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";


    private String verifyEmailRequest = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soap:Body>\n" +
            "    <AdvancedVerifyEmail xmlns=\"http://ws.cdyne.com/\">\n" +
            "      <email>string</email>\n" +
            "      <timeout>int</timeout>\n" +
            "      <LicenseKey>string</LicenseKey>\n" +
            "    </AdvancedVerifyEmail>\n" +
            "  </soap:Body>\n" +
            "</soap:Envelope>";


    /**
     * Evaluate if the library can parser correctly the elements in the WSDL document
     */
    @Test(enabled = false)
    public void parseWsdlContent() {

        soap = new SoapServiceUtils();
        soap.parseWsdl("http://www.dneonline.com/calculator.asmx?WSDL");

        assertThat(soap.getServiceName()).isEqualToIgnoringCase("Calculator");
        assertThat(soap.getPortName()).isEqualToIgnoringCase("CalculatorSoap");
        assertThat(soap.getTargetNameSpace()).isEqualToIgnoringCase("http://tempuri.org/");

        assertThat(soap.getAvailableSoapActions().get("Add")).isEqualToIgnoringCase("http://tempuri.org/Add");
        assertThat(soap.getAvailableSoapActions().get("Subtract")).isEqualToIgnoringCase("http://tempuri.org/Subtract");
        assertThat(soap.getAvailableSoapActions().get("Multiply")).isEqualToIgnoringCase("http://tempuri.org/Multiply");
        assertThat(soap.getAvailableSoapActions().get("Divide")).isEqualToIgnoringCase("http://tempuri.org/Divide");
    }

    /**
     * Evaluatethe function to read tags from an XML string
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    @Test(enabled = false)
    public void evaluateXmlTest() throws IOException, SAXException, ParserConfigurationException {

        soap = new SoapServiceUtils();
        assertThat(soap.evaluateXml(this.addRequest, "intA")).isEqualToIgnoringCase("int");
        assertThat(soap.evaluateXml(this.addRequest, "intB")).isEqualToIgnoringCase("int");
        assertThat(soap.evaluateXml(this.addRequest, "intC")).isNull();

    }

    /**
     * Test the function Add of the webservice in http://www.dneonline.com/calculator.asmx
     * and verify if the response is correct
     * @throws Exception
     */
    @Test(enabled = false)
    public void executeRemoteMethodAdd() throws Exception {

        String response;
        Map<String, String> map = new LinkedHashMap<>();
        soap = new SoapServiceUtils();
        soap.parseWsdl("http://www.dneonline.com/calculator.asmx?WSDL");

        map.put("intA", "2");
        map.put("intB", "2");
        response = soap.executeMethodWithParams("Add", this.addRequest, map);
        assertThat(soap.evaluateXml(response, "AddResult")).matches("4");

        map.put("intA", "5");
        map.put("intB", "5");
        response = soap.executeMethodWithParams("Add", this.addRequest, map);
        assertThat(soap.evaluateXml(response, "AddResult")).matches("10");
    }

    /**
     * Test the function Add of the webservice in http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx
     * and verify if the response is correct
     * @throws Exception
     */
    @Test(enabled = false)
    public void executeRemoteMethodEmail() throws Exception {

        String response;
        Map<String, String> map = new LinkedHashMap<>();
        soap = new SoapServiceUtils();
        soap.parseWsdl("http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx?WSDL");

        map.put("email", "josefd8@gmail.com");
        map.put("timeout", "0");
        map.put("LicenseKey", "");
        response = soap.executeMethodWithParams("AdvancedVerifyEmail", this.verifyEmailRequest, map);
        assertThat(soap.evaluateXml(response, "GoodEmail")).matches("true");

        map.put("email", "novalidemail");
        map.put("timeout", "0");
        map.put("LicenseKey", "");
        response = soap.executeMethodWithParams("AdvancedVerifyEmail", this.verifyEmailRequest, map);
        assertThat(soap.evaluateXml(response, "GoodEmail")).matches("false");

    }
}
