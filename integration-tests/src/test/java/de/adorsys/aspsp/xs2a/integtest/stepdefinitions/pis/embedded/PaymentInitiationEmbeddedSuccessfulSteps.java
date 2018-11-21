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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;

import cucumber.api.java.en.Then;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PaymentInitiationSctJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class PaymentInitiationEmbeddedSuccessfulSteps {

    @Autowired
    private Context<PaymentInitiationSctJson, PaymentInitationRequestResponse201> context;

//    @Given("^PSU wants to initiate a single payment (.*) using the payment service (.*) and the payment product (.*)$")
//    See GlobalSuccessfulSteps

//    @When("^PSU sends the single payment initiating request$")
//    See GlobalSuccessfulSteps

    @Then("^a successful response code and the appropriate authentication URL is delivered to the PSU$")
    public void checkResponseCodeAndAuthorisationLink() {
        ResponseEntity<PaymentInitationRequestResponse201> actualResponse = context.getActualResponse();
        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        //TODO to be check
        //assertThat(actualResponse.getBody().getLinks().get("startAuthorisationWithPsuAuthentication"), notNullValue());
    }
}
