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
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.model.TppMessages;
import de.adorsys.psd2.model.TppMessages404;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

@FeatureFileSteps
public class ConsentStatusRequestErrorfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<Consents, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;
    @Given("^AISP wants to get the status of that consent and the data (.*) with not existing consent id$")
    public void loadTestData(String dataFileName) throws IOException {

        TestData<Consents, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/ais/consent/" + dataFileName, UTF_8),
            new TypeReference<TestData<Consents, TppMessages>>() {});

        context.setTestData(data);

    }

    @When("^AISP requests errorFull consent status$")
    public void sendErrorfulConsentRequest() throws HttpClientErrorException, IOException {
        HttpEntity entity = HttpEntityUtils.getHttpEntityWithoutBody(
            context.getTestData().getRequest(), context.getAccessToken());
        try {
            restTemplate.exchange(
                context.getBaseUrl() + "/consents/{consentId}/status",
                HttpMethod.GET,
                entity,
                TppMessages404.class,
                UUID.randomUUID().toString());
        } catch (RestClientResponseException rex) {
            context.handleRequestError(rex);
        }
    }

    //@Then("^an error response code is displayed and an appropriate error response is shown$")
    //See commonStep
}
