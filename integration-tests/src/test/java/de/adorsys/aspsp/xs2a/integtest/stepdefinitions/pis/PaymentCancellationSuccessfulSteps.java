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
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse200202;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class PaymentCancellationSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, PaymentInitiationCancelResponse200202> context;

    @Autowired
    private ObjectMapper mapper;

    @Given("PSU wants to cancel an existing payment (.*) using the payment service (.*)$")
    public void loadTestData(String dataFileName, String paymentService ) throws IOException {
        context.setPaymentService(paymentService);

        //TO DO: handle Testdata
        //context.setPaymentId("");

        TestData<HashMap, PaymentInitiationCancelResponse200202> data = mapper.readValue(resourceToString(
            "/data-input/pis/cancellation/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, PaymentInitiationCancelResponse200202>>() {
            });

        context.setTestData(data);
    }

    @When("^PSU initiates the cancellation of the payment$")
    public void sendPaymentCancellationRequest() {
        HttpEntity<HashMap> entity = PaymentUtils.getHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<PaymentInitiationCancelResponse200202> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId(),
            HttpMethod.DELETE,
            entity,
            PaymentInitiationCancelResponse200202.class);

        context.setActualResponse(response);

    }

    @Then("^an successful response code and the appropriate transaction status is delivered to the PSU$")
    public void checkResponse() {
        ResponseEntity<PaymentInitiationCancelResponse200202> actualResponse = context.getActualResponse();
        PaymentInitiationCancelResponse200202 givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getTransactionStatus(), equalTo(givenResponseBody.getTransactionStatus()));
    }
}
