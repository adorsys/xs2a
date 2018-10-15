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
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.ParseService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.RestCallService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;

@FeatureFileSteps
public class SinglePaymentErrorfulSteps {

    @Autowired
    private RestCallService restService;

    @Autowired
    private Context<PaymentInitiationSctJson, TppMessages> context;

    @Autowired
    private ParseService parseService;


    @Given("^PSU initiates an errorful single payment (.*) using the payment service (.*) and the payment product (.*)$")
    public void loadTestData(String dataFileName, String paymentService, String paymentProduct) throws IOException {
        context.setPaymentProduct(paymentProduct);
        context.setPaymentService(paymentService);

        parseService.parseJson("/data-input/pis/single/" + dataFileName,  new TypeReference<TestData<PaymentInitiationSctJson, TppMessages>>() {
        });
    }

    @When("^PSU sends the single payment initiating request with error$")
    public void sendPaymentInitiatingRequestWithError() throws HttpClientErrorException, IOException {
        restService.sendErrorfulRestCall(HttpMethod.POST,context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentProduct());
    }

    // @Then("^an error response code and the appropriate error response are received$")
    // See GlobalErrorfulSteps
}
