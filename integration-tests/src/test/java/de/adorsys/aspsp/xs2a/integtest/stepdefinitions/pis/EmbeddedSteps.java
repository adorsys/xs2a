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
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class EmbeddedSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<PaymentInitiationSctJson, LinksPaymentInitiation> context;

    @Autowired
    private ObjectMapper mapper;

    @Given("^PSU wants to initiate a payment using the payment service (.*) and the payment product (.*)$")
    public void psuWantsToInitiateAPayment(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        TestData<PaymentInitiationSctJson, LinksPaymentInitiation> data = mapper.readValue(resourceToString(
            "/data-input/pis/single/" + dataFileName, UTF_8),
            new TypeReference<TestData<PaymentInitiationSctJson, LinksPaymentInitiation>>() {
            });

        context.setTestData(data);
    }

    @When("^PSU sends the payment initiating request$")
    public void psuSendsThePaymentInitiatingRequest() {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<LinksPaymentInitiation> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct(),
            HttpMethod.POST,
            entity,
            LinksPaymentInitiation.class);

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate authentication URL is delivered to the PSU$")
    public void checkResponseCodeAndAuthorisationLink() {
        ResponseEntity<LinksPaymentInitiation> actualResponse = context.getActualResponse();
        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getStartAuthorisationWithPsuAuthentication(), notNullValue());
    }

    @Given("^PSU initiated a payment (.*) with the payment-id (.*)$")
    public void psuInitiatedAPaymentWithThePaymentId(String paymentId) {
        context.setPaymentId(paymentId);
        HttpEntity entity = PaymentUtils.getHttpEntity(null, context.getAccessToken());

        restTemplate.exchange(
            context.getBaseUrl() + context.getPaymentService() + "/" + paymentId + "/authorisations",
            HttpMethod.POST,
            entity,
            LinksSelectPsuAuthenticationMethod.class);
    }

    @And("^PSU needs to authorize and identify using (.*)$")
    public void psuAuthorizationUsingAuthorisationData(String dataFileName, String authorisationId) throws IOException {
        context.setAuthorisationId(authorisationId);
        TestData<HashMap, LinksPaymentInitiation> data = mapper.readValue(resourceToString(
            "/data-input/pis/embedded/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, TppMessages>>() {
            });

        HttpEntity entity = PaymentUtils.getHttpEntity(data.getRequest(), context.getAccessToken());

        restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentId() + "/authorisations" + authorisationId,
            HttpMethod.PUT,
            entity,
            StartScaprocessResponse.class);
    }

    @When("^PSU sends the authorisation request$")
    public void psuSendsTheAuthorisationRequest() {
        HttpEntity entityWithScaMethod = PaymentUtils.getHttpEntity("Sca-Method", context);

        restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentId() + "/authorisations" + context.getAuthorisationId(),
            HttpMethod.PUT,
            entityWithScaMethod,
            LinksSelectPsuAuthenticationMethod.class);

        HttpEntity entityWithTanValue = PaymentUtils.getHttpEntity("Tan-Value", context);
        ResponseEntity<ScaStatusResponse> finalScaStatus = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentId() + "/authorisations" + context.getAuthorisationId(),
            HttpMethod.PUT,
            entityWithTanValue,
            ScaStatusResponse.class);

        context.setAuthorisationResponse(finalScaStatus);
    }

    @Then("^a successful response code and the appropriate authorization data are received$")
    public void checkResponseCodeAndScaStatus() {
        ResponseEntity<ScaStatusResponse> actualResponse = context.getAuthorisationResponse();
        assertThat(actualResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(actualResponse.getBody().getScaStatus().toString(), equalTo(ScaStatus.PSUAUTHENTICATED.toString()));
    }
}
