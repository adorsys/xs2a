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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.common;

import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse200202;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class PaymentCancellationSuccessfulSteps {

    @Autowired
    private Context<HashMap, PaymentInitiationCancelResponse200202> context;

    @Autowired
    private TestService testService;

    //    @Given("^PSU sends the single payment initiation request and receives the paymentId$")
    //    See Global Successful Steps

    @And("PSU wants to cancel the payment by using a set of data (.*)$")
    public void loadPaymentCancellationTestData(String dataFileName) throws IOException {
        testService.parseJson("/data-input/pis/cancellation/" + dataFileName, new TypeReference<TestData<HashMap, PaymentInitiationCancelResponse200202>>() {
        });
    }

    @When("^PSU initiates the cancellation of the payment$")
    public void sendPaymentCancellationRequest() {
        testService.sendRestCall(HttpMethod.DELETE, context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId());
    }

    @Then("^an successful response code and the appropriate transaction status is delivered to the PSU$")
    public void checkResponse() {
        ResponseEntity<PaymentInitiationCancelResponse200202> actualResponse = context.getActualResponse();
        PaymentInitiationCancelResponse200202 givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getTransactionStatus(), equalTo(givenResponseBody.getTransactionStatus()));
        assertThat(actualResponse.getHeaders().get("x-request-id").get(0), equalTo(context.getTestData().getRequest().getHeader().get("x-request-id")));
    }
}
