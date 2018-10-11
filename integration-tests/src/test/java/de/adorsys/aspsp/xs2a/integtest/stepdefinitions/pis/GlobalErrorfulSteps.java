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

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
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

        assertThat(actualTppMessages.size(), is(equalTo(givenTppMessages.size())));

        for(int i = 0; i < actualTppMessages.size(); i++) {
            assertThat(actualTppMessages.get(i).getCategory(), equalTo(givenTppMessages.get(i).getCategory()));
            assertThat(actualTppMessages.get(i).getCode(), equalTo(givenTppMessages.get(i).getCode()));
        }
    }

    // @After **** see GlobalSuccessfulSteps
}
