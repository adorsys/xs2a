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
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


@FeatureFileSteps
public class GlobalErrorfulSteps {

    @Autowired
    private Context context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @And("^PSU prepares the errorful data (.*) with the payment service (.*)$")
    public void loadErrorfulUpdateAuthorisationData (String authorisationData, String paymentService) throws IOException {
        TestData<HashMap, TppMessages> data = mapper.readValue(resourceToString(
            "/data-input/pis/embedded/" + authorisationData, UTF_8),
            new TypeReference<TestData<HashMap, TppMessages>>() {
            });

        context.setTestData(data);
        context.setPaymentService(paymentService);
        final String NOT_EXISTING_PAYMENT_ID_FILE_NAME = "not-existing-paymentId";
        final String NOT_EXISTING_AUTHORISATION_ID_FILE_NAME = "wrong-authorisation-id";
        if (authorisationData.toLowerCase().contains(NOT_EXISTING_AUTHORISATION_ID_FILE_NAME.toLowerCase())){
            final String WRONG_AUTHORISATION_ID = "11111111-aaaa-xxxx-1111-1x1x1x1x1x1x";
            context.setAuthorisationId(WRONG_AUTHORISATION_ID);
        } else if (authorisationData.toLowerCase().contains(NOT_EXISTING_PAYMENT_ID_FILE_NAME.toLowerCase())) {
            final String WRONG_PAYMENT_ID = "11111111-aaaa-xxxx-1111-1x1x1x1x1x1x";
            context.setPaymentId(WRONG_PAYMENT_ID);
        }
    }

    @When("^PSU sends the errorful update authorisation data request$")
    public void sendUpdateAuthorisationWithIdentificationRequest() throws IOException {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());
        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/" + context.getAuthorisationId(),
                HttpMethod.PUT,
                entity,
                HashMap.class);
        } catch (RestClientResponseException restClientException) {
            context.handleRequestError(restClientException);
        }
    }

    @Then("^an error response code and the appropriate error response are received")
    public void anErrorResponseCodeIsDisplayedTheAppropriateErrorResponse() {
        TppMessages actualTppMessages = context.getTppMessages();
        TppMessages givenTppMessages = (TppMessages) context.getTestData().getResponse().getBody();

        HttpStatus httpStatus = context.getTestData().getResponse().getHttpStatus();
        assertThat(context.getActualResponseStatus(), equalTo(httpStatus));

        assertThat(actualTppMessages, is(equalTo(givenTppMessages)));

        //TODO: refactor to normal for loop
        actualTppMessages.forEach ((msg) -> {
            assertThat(msg.getCategory().toString(), equalTo(givenTppMessages.get(msg.getCategory().ordinal()).getCategory().toString()));
            assertThat(msg.getCode().toString(), equalTo(givenTppMessages.get(msg.getCategory().ordinal()).getCode().toString()));
        });
    }

    // @After **** see GlobalSuccessfulSteps
}
