package de.adorsys.aspsp.xs2a.integtest.stepdefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class PISSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<SinglePayments, HashMap, PaymentInitialisationResponse> context;

    /* see GlobalSteps.java
        @Given("^PSU is logged in$")
    */

    /* see GlobalSteps.java
        @And("^(.*) approach is used$")
    */

    @And("^PSU wants to initiate a single payment (.*) using the payment product (.*)$")
    public void loadTestData(String dataFileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);

        File jsonFile = new File("src/test/resources/data-input/pis/single/" + dataFileName);

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        TestData<SinglePayments, HashMap> data = mapper.readValue(jsonFile, new TypeReference<TestData<SinglePayments, HashMap>>() {
        });

        context.setTestData(data);
    }

    @When("^PSU sends the single payment initiating request$")
    public void sendPaymentInitiatingRequest() {
        HttpEntity<SinglePayments> entity = getSinglePaymentsHttpEntity();

        ResponseEntity<PaymentInitialisationResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            PaymentInitialisationResponse.class);

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate single payment response data$")
    public void checkResponseCode() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();
        Map givenResponseBody = context.getTestData().getResponse().getBody();

        HttpStatus compareStatus = convertStringToHttpStatusCode(context.getTestData().getResponse().getCode());
        assertThat(actualResponse.getStatusCode(), equalTo(compareStatus));

        assertThat(actualResponse.getBody().getTransactionStatus().name(), equalTo(givenResponseBody.get("transactionStatus")));
        assertThat(actualResponse.getBody().getPaymentId(), notNullValue());
    }

    @And("^a redirect URL is delivered to the PSU$")
    public void checkRedirectUrl() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().getLinks().getScaRedirect(), notNullValue());
    }

    private HttpStatus convertStringToHttpStatusCode(String code){
        return HttpStatus.valueOf(Integer.valueOf(code));
    }

    @When("^PSU sends the single payment initiating request with error$")
    public void sendPaymentInitiatingRequestWithError() throws HttpClientErrorException {
        HttpEntity<SinglePayments> entity = getSinglePaymentsHttpEntity();

        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
                HttpMethod.POST,
                entity,
                HashMap.class);
        } catch (HttpClientErrorException hce) {
            ResponseEntity<PaymentInitialisationResponse> actualResponse = new ResponseEntity<>(hce.getStatusCode());
            context.setActualResponse(actualResponse);
        }
    }

    @Then("^an error response code is displayed the appropriate error response$")
    public void anErrorResponseCodeIsDisplayedTheAppropriateErrorResponse() {
        ResponseEntity<PaymentInitialisationResponse> response = context.getActualResponse();
        HttpStatus httpStatus = convertStringToHttpStatusCode(context.getTestData().getResponse().getCode());
        assertThat(response.getStatusCode(), equalTo(httpStatus));
    }

    private HttpEntity<SinglePayments> getSinglePaymentsHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        return new HttpEntity<>(context.getTestData().getRequest().getBody(), headers);
    }
}
