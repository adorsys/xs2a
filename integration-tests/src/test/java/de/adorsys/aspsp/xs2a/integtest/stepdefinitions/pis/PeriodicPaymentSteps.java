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
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.integtest.entities.ITMessageError;
import de.adorsys.aspsp.xs2a.integtest.entities.ITPeriodicPayments;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class PeriodicPaymentSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<ITPeriodicPayments, HashMap, PaymentInitialisationResponse> context;

    @Autowired
    private ObjectMapper mapper;

    @And("^PSU wants to initiate a recurring payment (.*) using the payment product (.*)$")
    public void loadTestDataForPeriodicPayment(String dataFileName, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);

        TestData<ITPeriodicPayments, HashMap> data = mapper.readValue(resourceToString("/data-input/pis/recurring/" + dataFileName, UTF_8), new TypeReference<TestData<ITPeriodicPayments, HashMap>>() {
        });

        context.setTestData(data);

        //TODO to be solved in a good manner
        context.getTestData().getRequest().getBody().setEndDate(LocalDate.now().plusDays(100));
    }

    //TODO Uncomment after ISO DateTime format rework
    /*@When("^PSU sends the recurring payment initiating request$")
    public void sendPeriodicPaymentInitiatingRequest() {
        HttpEntity<ITPeriodicPayments> entity = PaymentUtils.getPaymentsHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<PaymentInitialisationResponse> responseEntity = restTemplate.exchange(
            context.getBaseUrl() + "/periodic-payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<PaymentInitialisationResponse>() {
            });

        context.setActualResponse(responseEntity);
    }*/

    @Then("^a successful response code and the appropriate recurring payment response data")
    public void checkResponseCodeFromPeriodicPayment() {
        Map responseBody = context.getTestData().getResponse().getBody();
        ResponseEntity<PaymentInitialisationResponse> responseEntity = context.getActualResponse();
        HttpStatus expectedStatus = context.getTestData().getResponse().getHttpStatus();
        assertThat(responseEntity.getStatusCode(), equalTo(expectedStatus));
        assertThat(responseEntity.getBody().getTransactionStatus().name(), equalTo(responseBody.get("transactionStatus")));
        assertThat(responseEntity.getBody().getPaymentId(), notNullValue());
    }

    @When("^PSU sends the recurring payment initiating request with error$")
    public void sendFalsePeriodicPaymentInitiatingRequest() throws IOException {
        HttpEntity<ITPeriodicPayments> entity = PaymentUtils.getPaymentsHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/periodic-payments/" + context.getPaymentProduct(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<PaymentInitialisationResponse>() {
                });

        } catch (HttpClientErrorException hce) {
            ResponseEntity<PaymentInitialisationResponse> actualResponse = new ResponseEntity<>(
                hce.getStatusCode());
            context.setActualResponse(actualResponse);

            ITMessageError messageError = mapper.readValue(hce.getResponseBodyAsString(), ITMessageError.class);
            context.setMessageError(messageError);
        }
    }

    /*
     * @Then("^an error response code is displayed the appropriate error response$")
     * see SinglePaymentSteps.java
     */
}
