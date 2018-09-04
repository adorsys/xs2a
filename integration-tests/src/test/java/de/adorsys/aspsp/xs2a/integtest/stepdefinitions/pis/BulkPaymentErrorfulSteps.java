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
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.BulkPaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    @Given("^PSU loads errorful multiple payments (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestDataForErrorfulBulkPayment(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        TestData<BulkPaymentInitiationSctJson, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/pis/bulk/" + dataFileName, UTF_8),
            new TypeReference<TestData<BulkPaymentInitiationSctJson, TppMessages>>() {});

        context.setTestData(data);
    }

    @When("^PSU sends the bulk payment initiating request with error$")
    public void sendBulkPaymentInitiatingRequest() throws IOException {
        HttpEntity<BulkPaymentInitiationSctJson> entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct(),
                HttpMethod.POST, entity, Object.class);
        } catch (RestClientResponseException restclientResponseException) {
            context.handleRequestError(restclientResponseException);
        }
    }
}
