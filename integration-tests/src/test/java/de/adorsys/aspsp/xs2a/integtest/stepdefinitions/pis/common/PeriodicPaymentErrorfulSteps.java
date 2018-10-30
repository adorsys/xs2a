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
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.PeriodicPaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.time.LocalDate;

@FeatureFileSteps
public class PeriodicPaymentErrorfulSteps {

    private static final long DAYS_OFFSET = 100L;

    @Autowired
    private Context<PeriodicPaymentInitiationSctJson, TppMessages> context;

    private String dataFileName;

    @Autowired
    private TestService testService;

    @And("^PSU loads an errorful recurring payment (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestDataForErrorfulPeriodicPayment(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);
        this.dataFileName = dataFileName;
        testService.parseJson("/data-input/pis/recurring/" + dataFileName, new TypeReference<TestData<PeriodicPaymentInitiationSctJson, TppMessages>>() {
        });
        context.getTestData().getRequest().getBody().setEndDate(LocalDate.now().plusDays(DAYS_OFFSET));
    }

    @When("^PSU sends the recurring payment initiating request with error$")
    public void sendFalsePeriodicPaymentInitiatingRequest() throws IOException {
        HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        if (dataFileName.contains("end-date-before-start-date")) {
            makeEndDateBeforeStartDate(entity);
        }
        testService.sendErrorfulRestCall(HttpMethod.POST,context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct(), entity);
    }

    private void makeEndDateBeforeStartDate(HttpEntity<PeriodicPaymentInitiationSctJson> entity) {
        entity.getBody().setEndDate(entity.getBody().getStartDate().minusDays(DAYS_OFFSET));
    }

    // @Then("^an error response code and the appropriate error response are received$")
    // See GlobalErrorfulSteps
}
