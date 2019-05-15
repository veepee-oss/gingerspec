package com.privalia.qa.specs;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import gherkin.ast.DocString;
import io.cucumber.datatable.DataTable;

public class GlobalGSpec extends BaseGSpec {

    private String today;

    private String actualAnswer;

    @Given("^today is Sunday$")
    public void today_is_Sunday() {
        System.out.println("1");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("^I ask whether it's Friday yet$")
    public void i_ask_whether_is_s_Friday_yet() {
        System.out.println("2");
    }

    @And("^This is a step with variable '(.+?)'$")
    public void thisIsAStepWithVariableVariable(String argument) throws Throwable {
        System.out.println("3");
    }
}
