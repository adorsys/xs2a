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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.ais;

import com.fasterxml.jackson.core.type.TypeReference;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.StartScaprocessResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import java.io.IOException;
import java.util.HashMap;

@Slf4j
@FeatureFileSteps
public class AuthorisationStartConsentCreationSuccessfulSteps {

    @Autowired
    private Context context;

    @Autowired
    private TestService testService;

    //Given PSU wants to create a consent <consent-id>
    // And PSU sends the create consent request
    // see successful steps

    @And("^PSU wants to start the authorisation for consent creation using the authorisation data (.*)$")
    public void loadAuthorisationData(String authorisationData) throws IOException {
        testService.parseJson("/data-input/ais/embedded/" + authorisationData, new TypeReference<TestData<HashMap, StartScaprocessResponse>>() {
        });
    }

    @When("^PSU sends the start authorisation request for consent creation$")
    public void sendStartAuthorisationRequestForConsent(){
        testService.sendRestCall(HttpMethod.POST,context.getBaseUrl()+"/consents/"+context.getConsentId()
        +"/authorisations");
    }

    //Then PSU checks if a link is received and the SCA status is correct
    // see Startauthorisation successful steps
}
