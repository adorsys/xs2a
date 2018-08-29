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
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.BulkPaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

@FeatureFileSteps
public class BulkPaymentErrorfulSteps {
    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<BulkPaymentInitiationSctJson, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;

    @Given("^PSU initiates errorful multiple payments (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestDataWithErrorBulkPayment(String dataFileName, String paymentProduct, String paymentService) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        TestData<BulkPaymentInitiationSctJson, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/pis/bulk/" + dataFileName, UTF_8),
            new TypeReference<TestData<BulkPaymentInitiationSctJson, TppMessages>>() {});

        context.setTestData(data);
    }

    @When("^PSU sends the bulk payment initiating request with error$")
    public void sendBulkPaymentInitiatingRequest() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        try {
            ResponseEntity<TppMessages> response = restTemplate.exchange(
                context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct(),
                HttpMethod.POST, new HttpEntity<>(context.getTestData().getRequest().getBody(), headers), new ParameterizedTypeReference<TppMessages>() {
                });

            context.setActualResponse(response);
        } catch (RestClientResponseException restclientResponseException) {
            handleRequestError(restclientResponseException);
        }
    }

    private void handleRequestError(RestClientResponseException exceptionObject) throws IOException {
        context.setActualResponseStatus(HttpStatus.valueOf(exceptionObject.getRawStatusCode()));
        String responseBodyAsString = exceptionObject.getResponseBodyAsString();
        TppMessages tppMessages = mapper.readValue(responseBodyAsString, TppMessages.class);
        context.setTppmessage(tppMessages);
    }
}
