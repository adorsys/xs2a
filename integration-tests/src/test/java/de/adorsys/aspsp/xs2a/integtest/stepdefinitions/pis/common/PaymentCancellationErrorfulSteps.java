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
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.AbstractErrorfulSteps;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.HashMap;

@FeatureFileSteps
public class PaymentCancellationErrorfulSteps extends AbstractErrorfulSteps {

    @Autowired
    private Context<HashMap, TppMessages> context;

    @Autowired
    private TestService testService;

    //    @Given("^PSU sends the single payment initiation request and receives the paymentId$")
    //    See Global Successful Steps

    @And("^PSU prepares the errorful cancellation request data (.*) with the payment service (.*)$")
    public void loadErrorfulPaymentCancellationTestData(String dataFileName, String paymentService) throws IOException {
        testService.parseJson("/data-input/pis/cancellation/" + dataFileName, new TypeReference<TestData<HashMap, TppMessages>>() {
        });
        context.setPaymentService(paymentService);
        this.setErrorfulIds(dataFileName);
    }

    @When("^PSU initiates the cancellation of the payment with error$")
    public void sendPaymentCancellationRequestWithError() throws HttpClientErrorException, IOException {
        testService.sendErrorfulRestCall(HttpMethod.DELETE, context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId());
    }

    // @Then("^an error response code and the appropriate error response are received$")
    // See GlobalErrorfulSteps

}
