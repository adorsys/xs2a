package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.consent.api.ConsentStatus;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import javassist.bytecode.stackmap.BasicBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;


import static org.apache.commons.io.IOUtils.resourceToString;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@FeatureFileSteps
public class ConsentRequestSteps {


    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<CreateConsentReq, HashMap, CreateConsentResp> context;


    @Autowired
    private ObjectMapper mapper;


    @Given("^PSU wants to create a consent (.*)$")


    public void loadTestData(String dataFileName) throws IOException {

        TestData<CreateConsentReq, HashMap> data = mapper.readValue(resourceToString("/data-input/ais/consent/" + dataFileName, UTF_8), new TypeReference<TestData<CreateConsentReq, HashMap>>() {
        });

        context.setTestData(data);
    }

    @When("^PSU sends the create consent request$")
    public void sendConsentRequest() {

        HttpEntity<CreateConsentReq> entity = getConsentRequestHttpEntity();

        ResponseEntity<CreateConsentResp> response = restTemplate.exchange(
            context.getBaseUrl() + "/consents",
            HttpMethod.POST,
            entity,
            CreateConsentResp.class);

        context.setActualResponse(response);
    }


    @Then("^a successful response code and the appropriate consent response data is delivered to the PSU$")
    public void checkResponseCode() {
        ResponseEntity<CreateConsentResp> actualResponse = context.getActualResponse();
        Map givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));

        assertThat(actualResponse.getBody().getConsentStatus().name(), equalTo(givenResponseBody.get("transactionStatus")));
        assertThat(actualResponse.getBody().getConsentId(), notNullValue());


    }

    private org.springframework.http.HttpEntity<CreateConsentReq> getConsentRequestHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        return new HttpEntity<>(context.getTestData().getRequest().getBody(), headers);
    }

}
