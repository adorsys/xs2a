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
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class UpdateAuthorisationWithScaSelectionSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context context;

    @Autowired
    private ObjectMapper mapper;

    //  @Given("^PSU wants to initiate a single payment (.*) using the payment service (.*) and the payment product (.*)$")
    // See SinglePaymentSuccessfulSteps

    // @And("^PSU sends the single payment initiating request and receives the paymentId$")
    // See GlobalSuccessfulSteps

    // @And("^PSU sends the start authorisation request and receives the authorisationId$")
    // See GlobalSuccessfulSteps

    // @And("^PSU wants to update the resource with his (.*)$")
    // See GlobalSuccessfulSteps

    // @And("^PSU sends the update identification data request$")
    // See UpdateAuthorisationWithIdentificationSuccessfulSteps

    @And("^PSU wants to select the authentication method using the (.*)$")
    public void loadScaMethodSelectionData(String filename) throws IOException {
        TestData<SelectPsuAuthenticationMethod, SelectPsuAuthenticationMethodResponse> data = mapper.readValue(resourceToString(
            "/data-input/pis/embedded/" + filename, UTF_8),
            new TypeReference<TestData<SelectPsuAuthenticationMethod, SelectPsuAuthenticationMethodResponse>>() {
            });

        context.setTestData(data);
    }

    @When("^PSU sends the select sca method request$")
    public void sendUpdateAuthorisationWithScaSelectionRequest() {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<SelectPsuAuthenticationMethodResponse> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/" + context.getAuthorisationId(),
            HttpMethod.PUT,
            entity,
            SelectPsuAuthenticationMethodResponse.class);

        context.setActualResponse(response);
    }

    @Then("PSU checks if the correct SCA status and response code is received for the selection$")
    public void checkScaStatusAndResponseCode() {
        ResponseEntity<SelectPsuAuthenticationMethodResponse> actualResponse = context.getActualResponse();
        SelectPsuAuthenticationMethodResponse givenResponseBody = (SelectPsuAuthenticationMethodResponse) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(givenResponseBody.getScaStatus()));
    }
}
