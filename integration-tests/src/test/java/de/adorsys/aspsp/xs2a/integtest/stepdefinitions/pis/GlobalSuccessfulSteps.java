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
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import de.adorsys.aspsp.xs2a.integtest.config.AuthConfigProperty;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@FeatureFileSteps
public class GlobalSuccessfulSteps {
    @Autowired
    private Context context;

    @Autowired
    @Qualifier("aspsp-mock")
    private RestTemplate template;

    @Autowired
    private AuthConfigProperty authConfigProperty;

    @Autowired
    private TestService testService;

    @Before
    public void loadTestDataIntoDb() {
        template.exchange(
            context.getMockUrl() + "/integration-tests/refresh-testing-data",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            String.class);
    }

    @Given("^PSU request access token for oauth approach$")
    public void requestAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", authConfigProperty.getGrantType());
        map.add("username", authConfigProperty.getUsername());
        map.add("password", authConfigProperty.getPassword());
        map.add("client_id", authConfigProperty.getClientId());
        map.add("client_secret", authConfigProperty.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<HashMap> response = null;
        try {
            response = template.exchange(authConfigProperty.getUrl(), HttpMethod.POST, entity, HashMap.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }

        context.setScaApproach("oauth");
        context.setAccessToken(Objects.requireNonNull(response).getBody().get("access_token").toString());
    }

    @And("^a redirect URL is delivered to the PSU$")
    public void checkRedirectUrl() {
        ResponseEntity<PaymentInitationRequestResponse201> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().getLinks().get("scaRedirect"), notNullValue());
    }

    @Then("^a successful response code and the appropriate payment response data are received$")
    public void checkResponseCode() {
        ResponseEntity<PaymentInitationRequestResponse201> actualResponse = context.getActualResponse();
        PaymentInitationRequestResponse201 givenResponseBody =  (PaymentInitationRequestResponse201) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));

        assertThat(actualResponse.getBody().getTransactionStatus(), equalTo(givenResponseBody.getTransactionStatus()));
        assertThat(actualResponse.getBody().getPaymentId(), notNullValue());

        // TODO: Take asserts back in when respective response headers are implemented (https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/289)
        // assertThat(actualResponse.getHeaders().get("Location"), equalTo(context.getBaseUrl() + "/" +
        //    context.getPaymentService() + "/" + actualResponse.getAspspConsentData().getPaymentId()));

        // assertThat(actualResponse.getHeaders().get("X-Request-ID"), equalTo(context.getTestData().getRequest().getHeader().get("x-request-id")));
    }

    // Embedded Global Step Payment Initiation
    @And("^PSU sends the single payment initiating request and receives the paymentId$")
    public void sendSinglePaymentInitiationEmbedded() {
        testService.sendRestCall(HttpMethod.POST, context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct());
        context.setPaymentId(((PaymentInitationRequestResponse201) context.getActualResponse().getBody()).getPaymentId());
    }

    // Embedded Global Step Payment Initiation
    @And("^PSU sends the start authorisation request and receives the authorisationId$")
    public void startAuthorisationRequest() throws IOException {
        HttpEntity entity = PaymentUtils.getHttpEntityWithoutBody(context.getTestData().getRequest(), context.getAccessToken());
        testService.parseJson(("/data-input/pis/embedded/" + "startAuth-successful.json"), new TypeReference<TestData<HashMap, StartScaprocessResponse>>() {
        });
        testService.sendRestCall(HttpMethod.POST, context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations", entity);
        extractAuthorisationId(context.getActualResponse());
    }

    private void extractAuthorisationId(ResponseEntity<StartScaprocessResponse> response) {
        String regex = "\\/authorisations\\/(.*?)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher((CharSequence) response.getBody().getLinks().get("startAuthorisationWithPsuAuthentication"));
        while(matcher.find()) {
            context.setAuthorisationId(matcher.group(1));
        }
    }

    // Embedded Global Step Payment Initiation
    @And("^PSU wants to update the resource with his (.*)$")
    public void loadIdentificationData(String identificationData) throws IOException {
        testService.parseJson("/data-input/pis/embedded/" + identificationData,  new TypeReference<TestData<UpdatePsuAuthentication, UpdatePsuAuthenticationResponse>>() {
        });
    }

    // Embedded Global Step Payment Initiation
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

    @After
    public void afterScenario() {
        log.debug("Cleaning up context");
        context.cleanUp();
    }
}
