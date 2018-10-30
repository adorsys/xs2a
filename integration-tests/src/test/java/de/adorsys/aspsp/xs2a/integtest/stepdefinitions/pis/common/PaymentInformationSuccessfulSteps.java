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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.BulkPaymentInitiationSctWithStatusResponse;
import de.adorsys.psd2.model.PaymentInitiationSctWithStatusResponse;
import de.adorsys.psd2.model.PeriodicPaymentInitiationSctWithStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class PaymentInformationSuccessfulSteps {

    private static final long DAYS_OFFSET = 100L;

    @Autowired
    private Context<HashMap, PaymentInitiationSctWithStatusResponse> context;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestService testService;

    //    @Given("^PSU sends the single payment initiation request and receives the paymentId$")
    //    See Global Successful Steps


    @And("^PSU wants to request the payment information by using a set of data (.*)$")
    public void loadPaymentInformationTestData(String dataFileName) throws IOException {
        TestData data;
        switch (context.getPaymentService()) {
            case "bulk-payments":
                data = mapper.readValue(resourceToString(
                    "/data-input/pis/information/" + dataFileName, UTF_8),new TypeReference<TestData<HashMap, BulkPaymentInitiationSctWithStatusResponse>>() {
                });

                break;
            case "periodic-payments" :
                 TestData<HashMap, PeriodicPaymentInitiationSctWithStatusResponse> dataPeriodic = mapper.readValue(resourceToString(
                    "/data-input/pis/information/" + dataFileName, UTF_8),new TypeReference<TestData<HashMap, PeriodicPaymentInitiationSctWithStatusResponse>>() {
                 });
                 dataPeriodic.getResponse().getBody().setEndDate(LocalDate.now().plusDays(DAYS_OFFSET));
                 data = dataPeriodic;
                break;

            default:
                 data = mapper.readValue(resourceToString(
                    "/data-input/pis/information/" + dataFileName, UTF_8),new TypeReference<TestData<HashMap, PaymentInitiationSctWithStatusResponse>>() {
                 });
                break;
        }
        context.setTestData(data);
    }

    //Default Case: Single Payment Information
    @When("^PSU requests the information of the payment$")
    public void sendPaymentInformationRequest() throws HttpClientErrorException {
        testService.sendRestCall(HttpMethod.GET,context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId());
    }

    @Then("^a successful response code and the payment information is delivered to the PSU$")
    public void checkResponseCodeAndPaymentInformation() {
        PaymentInitiationSctWithStatusResponse givenResponseBody = context.getTestData().getResponse().getBody();
        ResponseEntity<PaymentInitiationSctWithStatusResponse> actualResponseEntity = context.getActualResponse();
        HttpStatus expectedStatus = context.getTestData().getResponse().getHttpStatus();

        assertThat(actualResponseEntity.getStatusCode(), equalTo(expectedStatus));

        assertThat(actualResponseEntity.getBody(), equalTo(givenResponseBody));
    }


}
