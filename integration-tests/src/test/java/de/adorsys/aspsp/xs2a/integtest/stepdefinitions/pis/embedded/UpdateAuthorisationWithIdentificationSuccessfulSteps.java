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

import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.ScaMethods;
import de.adorsys.psd2.model.ScaStatus;
import de.adorsys.psd2.model.UpdatePsuAuthentication;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class UpdateAuthorisationWithIdentificationSuccessfulSteps {

    @Autowired
    private TestService testService;
    @Autowired
    private Context context;

    // @Given("^PSU sends the single payment initiation request and receives the paymentId$")
    // See Global Successful Steps

    // @And("^PSU sends the start authorisation request and receives the authorisationId$")
    // See GlobalSuccessfulSteps

    @And("^PSU wants to update the resource with his identification data (.*)$")
    public void loadIdentificationData(String identificationData) throws IOException {
        testService.parseJson("/data-input/pis/embedded/" + identificationData,  new TypeReference<TestData<UpdatePsuAuthentication, UpdatePsuAuthenticationResponse>>() {
        });
    }

    @When("^PSU sends the update identification data request$")
    public void sendUpdateAuthorisationWithIdentificationRequest() {
        testService.sendRestCall(HttpMethod.PUT,context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/" + context.getAuthorisationId());
    }

    @Then("PSU checks if the correct SCA status and response code is received$")
    public void checkScaStatusAndResponseCode() {
        ResponseEntity<UpdatePsuAuthenticationResponse> actualResponse = context.getActualResponse();
        UpdatePsuAuthenticationResponse givenResponseBody = (UpdatePsuAuthenticationResponse) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));

        if (actualResponse.getBody().getScaStatus().equals(ScaStatus.PSUAUTHENTICATED)) {
            ScaMethods actualMethods = actualResponse.getBody().getScaMethods();

            assertThat(actualMethods.size(), equalTo(givenResponseBody.getScaMethods().size()));

            for (int i = 0; i < actualMethods.size(); i++) {
                assertThat(actualMethods.get(i).getAuthenticationType(), equalTo(givenResponseBody.getScaMethods().get(i).getAuthenticationType()));
                assertThat(actualMethods.get(i).getAuthenticationMethodId(), notNullValue());
            }
        }
    }
}
