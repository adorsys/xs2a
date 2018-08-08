package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

@FeatureFileSteps
public class ConsentRequestSteps {
    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;


    @Given("^PSU wants to create a consent(.*)$")
    public void loadTestData(String dataFileName) throws IOException {
        ///TestData<ConsentStatus,HashMap> data =
    }
    @When("^PSU sends the create consent request$")
    public void sendConsentRequest (){

    }
    @Then("^a successful response code and the appropriate consent response data is delivered to the PSU$")
    public void checkResponseCode() {

    }


}
