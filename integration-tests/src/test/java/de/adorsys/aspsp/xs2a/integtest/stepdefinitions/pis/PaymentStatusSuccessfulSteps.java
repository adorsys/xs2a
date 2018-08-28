/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitiationStatusResponse200Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class PaymentStatusSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context< HashMap, PaymentInitiationStatusResponse200Json> context;

    @Autowired
    private ObjectMapper mapper;

    @Given("^Psu wants to request the payment status of a payment with payment-id (.*) by using the payment-service (.*)$")
    public void setPaymentParameters(String paymentId, String paymentService) {
        context.setPaymentId(paymentId);
        context.setPaymentService(paymentService);
    }

    @And("^the set of data (.*)$")
    public void loadTestData(String dataFileName) throws IOException {
        TestData<HashMap, PaymentInitiationStatusResponse200Json> data = mapper.readValue(resourceToString("/data-input/pis/status/" + dataFileName, UTF_8), new TypeReference<TestData<HashMap, PaymentInitiationStatusResponse200Json>>() {
        });

        context.setTestData(data);
    }

    @When("^PSU requests the status of the payment$")
    public void sendPaymentStatusRequest() throws HttpClientErrorException {
        HttpEntity<HashMap> entity = getStatusHttpEntity();

        ResponseEntity<PaymentInitiationStatusResponse200Json> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/status",
            HttpMethod.GET,
            entity,
            PaymentInitiationStatusResponse200Json.class);

        context.setActualResponse(response);
    }

    @Then("^an appropriate response code and the status is delivered to the PSU$")
    public void checkStatus() {
        ResponseEntity<PaymentInitiationStatusResponse200Json> actualResponse = context.getActualResponse();
        PaymentInitiationStatusResponse200Json givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getTransactionStatus().name(), equalTo(givenResponseBody.getTransactionStatus().name()));
    }

    private HttpEntity getStatusHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        return new HttpEntity<>(null, headers);
    }
}
