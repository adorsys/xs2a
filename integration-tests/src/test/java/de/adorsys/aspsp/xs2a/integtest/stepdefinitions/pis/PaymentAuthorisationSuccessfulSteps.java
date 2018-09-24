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

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.LinksPaymentInitiation;
import de.adorsys.psd2.model.LinksSelectPsuAuthenticationMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

@FeatureFileSteps
public class PaymentAuthorisationSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, LinksPaymentInitiation> context;

    @Given("^PSU initiated a payment (.*) with the payment-id (.*)$")
    public void psuInitiatedAPaymentWithThePaymentId(String paymentService, String paymentId) {
        HttpEntity entity = PaymentUtils.getHttpEntity(null, context.getAccessToken());

        ResponseEntity<LinksPaymentInitiation> response = restTemplate.exchange(
            context.getBaseUrl() + paymentService + "/" + paymentId + "/authorisations",
            HttpMethod.POST,
            entity,
            LinksPaymentInitiation.class);
        context.setActualResponse(response);
    }

    @And("^check if authorisationId and SCA status are valid$")
    public void checkAuthorisationIdAndScaStatus() {
        ResponseEntity<LinksPaymentInitiation> actualResponse = context.getActualResponse();
        LinksPaymentInitiation givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));
        assertThat(actualResponse.getBody().getStartAuthorisationWithPsuAuthentication(), not(isEmptyString()));
    }
}
