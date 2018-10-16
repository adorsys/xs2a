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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.redirect;

import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PaymentInitiationSctJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.io.IOException;

@FeatureFileSteps
public class SinglePaymentSuccessfulSteps {

    @Autowired
    private Context<PaymentInitiationSctJson, PaymentInitationRequestResponse201> context;


    @Autowired
    private TestService testService;

    @Given("^PSU wants to initiate a single payment (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestData(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        testService.parseJson("/data-input/pis/single/" + dataFileName,  new TypeReference<TestData<PaymentInitiationSctJson, PaymentInitationRequestResponse201>>() {
            });
    }

    @When("^PSU sends the single payment initiating request$")
    public void sendPaymentInitiatingRequest() {
          testService.sendRestCall(HttpMethod.POST,context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct());
    }

    // @Then("^a successful response code and the appropriate payment response data are received$")
    // see ./GlobalSuccessfulSteps.java

    // @And("^a redirect URL is delivered to the PSU$")
    // See GlobalSuccessfulSteps
}
