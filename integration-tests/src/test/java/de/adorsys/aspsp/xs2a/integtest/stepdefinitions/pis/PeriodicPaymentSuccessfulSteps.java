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
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PeriodicPaymentInitiationSctJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class PeriodicPaymentSuccessfulSteps {

    private static final long DAYS_OFFSET = 100L;

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<PeriodicPaymentInitiationSctJson, PaymentInitationRequestResponse201> context;

    @Autowired
    private ObjectMapper mapper;

    @And("^PSU wants to initiate a recurring payment (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestDataForSuccessfulPeriodicPayment(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        TestData<PeriodicPaymentInitiationSctJson, PaymentInitationRequestResponse201> data = mapper.readValue(
            resourceToString("/data-input/pis/recurring/" + dataFileName, UTF_8),
            new TypeReference<TestData<PeriodicPaymentInitiationSctJson, PaymentInitationRequestResponse201>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getBody().setEndDate(LocalDate.now().plusDays(DAYS_OFFSET));
    }

    @When("^PSU sends the recurring payment initiating request$")
    public void sendSuccessfulPeriodicPaymentInitiatingRequest() {
        HttpEntity<PeriodicPaymentInitiationSctJson> entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<PaymentInitationRequestResponse201> responseEntity = restTemplate.exchange(
            context.getBaseUrl() + "/periodic-payments/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<PaymentInitationRequestResponse201>() {
            });

        context.setActualResponse(responseEntity);
    }

    @Then("^a successful response code and the appropriate recurring payment response data")
    public void checkSuccessfulResponseCodeFromPeriodicPayment() {
        PaymentInitationRequestResponse201 responseBody = context.getTestData().getResponse().getBody();
        ResponseEntity<PaymentInitationRequestResponse201> responseEntity = context.getActualResponse();
        HttpStatus expectedStatus = context.getTestData().getResponse().getHttpStatus();

        assertThat(responseEntity.getStatusCode(), equalTo(expectedStatus));
        assertThat(responseEntity.getBody().getTransactionStatus().name(), equalTo(responseBody.getTransactionStatus()));
        assertThat(responseEntity.getBody().getPaymentId(), notNullValue());
    }
}
