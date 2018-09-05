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
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.PaymentInitiationSctWithStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

public class SinglePaymentInformationSuccessfulSteps {
    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, PaymentInitiationSctWithStatusResponse> context;

    @Autowired
    private ObjectMapper mapper;

    @Given("^Psu wants to request the payment information of a payment with payment-id (.*) by using the payment-service (.*)$")
    public void setPaymentParametersforRequestingPaymentInformation(String paymentId, String paymentService) {
        context.setPaymentId(paymentId);
        context.setPaymentService(paymentService);
    }

    @And("^the set of payment information data (.*)$")
    public void loadPaymentInformationTestData(String dataFileName) throws IOException {
        TestData<HashMap, PaymentInitiationSctWithStatusResponse> data = mapper.readValue(resourceToString(
            "/data-input/pis/status/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, PaymentInitiationSctWithStatusResponse>>() {
            });

        context.setTestData(data);
    }

    @When("^PSU requests the information of the payment$")
    public void sendPaymentInformationRequest() throws HttpClientErrorException {
        HttpEntity entity = PaymentUtils.getHttpEntity(context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<PaymentInitiationSctWithStatusResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId(),
            HttpMethod.GET,
            entity,
            PaymentInitiationSctWithStatusResponse.class);

        context.setActualResponse(response);
    }

}
