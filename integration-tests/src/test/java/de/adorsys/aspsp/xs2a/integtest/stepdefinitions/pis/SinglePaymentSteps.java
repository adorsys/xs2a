package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.integtest.entities.ITMessageError;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class SinglePaymentSteps {

    private static final long DAYS_OFFSET = 1L;
    private static final long HOURS_OFFSET = 2L;

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<SinglePayment, HashMap, PaymentInitialisationResponse> context;

    @Autowired
    private ObjectMapper mapper;

    private String dataFileName;

    @Given("^PSU wants to initiate a single payment (.*) using the payment product (.*)$")
    public void loadTestData(String dataFileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        this.dataFileName = dataFileName;

        TestData<SinglePayment, HashMap> data = mapper.readValue(resourceToString("/data-input/pis/single/" + dataFileName, UTF_8), new TypeReference<TestData<SinglePayment, HashMap>>() {
        });

        context.setTestData(data);
    }

    //TODO Uncomment after ISO DateTime format rework
    /*@When("^PSU sends the single payment initiating request$")
    public void sendPaymentInitiatingRequest() {
        HttpEntity<SinglePayment> entity = getSinglePaymentsHttpEntity();

        ResponseEntity<PaymentInitialisationResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            PaymentInitialisationResponse.class);

        context.setActualResponse(response);
    }*/

    @Then("^a successful response code and the appropriate single payment response data$")
    public void checkResponseCode() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();
        Map givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));

        assertThat(actualResponse.getBody().getTransactionStatus().name(), equalTo(givenResponseBody.get("transactionStatus")));
        assertThat(actualResponse.getBody().getPaymentId(), notNullValue());
    }

    @And("^a redirect URL is delivered to the PSU$")
    public void checkRedirectUrl() {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().getLinks().getScaRedirect(), notNullValue());
    }

    @When("^PSU sends the single payment initiating request with error$")
    public void sendPaymentInitiatingRequestWithError() throws HttpClientErrorException, IOException {
        HttpEntity<SinglePayment> entity = getSinglePaymentsHttpEntity();
        if (dataFileName.contains("expired-exec-date")) {
            makeDateAndTimeOffset(entity);
        }

        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/payments/" + context.getPaymentProduct(),
                HttpMethod.POST,
                entity,
                HashMap.class);
        } catch (RestClientResponseException rex) {
            handleRequestError(rex);
        }
    }

    private void makeDateAndTimeOffset(HttpEntity<SinglePayment> entity) {
        LocalDate dateOffset = context.getTestData().getRequest().getBody().getRequestedExecutionDate().minusDays(DAYS_OFFSET);
        LocalDateTime dateTimeOffset = context.getTestData().getRequest().getBody().getRequestedExecutionTime().minusDays(DAYS_OFFSET).minusHours(HOURS_OFFSET);
        entity.getBody().setRequestedExecutionDate(dateOffset);
        entity.getBody().setRequestedExecutionTime(dateTimeOffset);
    }

    private void handleRequestError(RestClientResponseException exceptionObject) throws IOException {
        ResponseEntity<PaymentInitialisationResponse> actualResponse = new ResponseEntity<>(HttpStatus.valueOf(exceptionObject.getRawStatusCode()));
        context.setActualResponse(actualResponse);
        String responseBodyAsString = exceptionObject.getResponseBodyAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        ITMessageError messageError = objectMapper.readValue(responseBodyAsString, ITMessageError.class);
        context.setMessageError(messageError);
    }

    @Then("^an error response code is displayed the appropriate error response$")
    public void anErrorResponseCodeIsDisplayedTheAppropriateErrorResponse() {
        ITMessageError givenErrorObject = context.getMessageError();
        Map givenResponseBody = context.getTestData().getResponse().getBody();

        HttpStatus httpStatus = context.getTestData().getResponse().getHttpStatus();
        assertThat(context.getActualResponse().getStatusCode(), equalTo(httpStatus));

        LinkedHashMap tppMessageContent = (LinkedHashMap) givenResponseBody.get("tppMessage");

        // for cases when transactionStatus and tppMessage created after request
        if (givenErrorObject.getTppMessage() != null) {
            assertThat(givenErrorObject.getTransactionStatus().name(), equalTo(givenResponseBody.get("transactionStatus")));
            assertThat(givenErrorObject.getTppMessage().getCategory().name(), equalTo(tppMessageContent.get("category")));
            assertThat(givenErrorObject.getTppMessage().getCode().name(), equalTo(tppMessageContent.get("code")));
        }
    }

    private HttpEntity<SinglePayment> getSinglePaymentsHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        return new HttpEntity<>(context.getTestData().getRequest().getBody(), headers);
    }
}
