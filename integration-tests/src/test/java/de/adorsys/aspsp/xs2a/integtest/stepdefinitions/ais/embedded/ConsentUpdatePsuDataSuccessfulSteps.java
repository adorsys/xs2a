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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.ais.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.And;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class ConsentUpdatePsuDataSuccessfulSteps {

    @Autowired
    private TestService testService;
    @Autowired
    private Context context;


    @And("^PSU sends the start consent authorisation request and receives the authorisationId$")
    public void startAuthorisationAndStoreId() throws IOException {
        HttpEntity entity = HttpEntityUtils.getHttpEntityWithoutBody(context.getTestData().getRequest(), context.getAccessToken());
        testService.parseJson(("/data-input/ais/embedded/" + "startAuth-successful.json"), new TypeReference<TestData<HashMap, StartScaprocessResponse>>() {
        });
        testService.sendRestCall(HttpMethod.POST, context.getBaseUrl() + "/consents/" + context.getConsentId() + "/authorisations", entity);
        ResponseEntity<StartScaprocessResponse> actualResponse = context.getActualResponse();

        String regex = "\\/authorisations\\/(.*?)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) actualResponse.getBody().getLinks().get("startAuthorisationWithPsuAuthentication"));
        while(matcher.find()) {
            context.setAuthorisationId(matcher.group(1));
        }
    }

    @And("^PSU wants to update the resource with his consent identification data (.*)$")
    public void loadIdentificationData(String identificationData) throws IOException {
        testService.parseJson("/data-input/ais/embedded/" + identificationData,  new TypeReference<TestData<UpdatePsuAuthentication, UpdatePsuAuthenticationResponse>>() {
        });

    }

    @When("^PSU sends the update consent identification data request$")
    public void sendUpdateAuthorisationWithIdentificationRequest() {
        testService.sendRestCall(HttpMethod.PUT,context.getBaseUrl() + "/consents/" + context.getConsentId() + "/authorisations/" + context.getAuthorisationId());
    }

    @Then("a successful response code and the appropriate link is delivered to the PSU$")
    public void checkResponseCodeAndLink () {
        ResponseEntity<UpdatePsuAuthenticationResponse> actualResponse = context.getActualResponse();
        UpdatePsuAuthenticationResponse givenResponseBody = (UpdatePsuAuthenticationResponse) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));
        assertThat(actualResponse.getBody().getScaMethods(), equalTo(givenResponseBody.getScaMethods()));

    }

}

