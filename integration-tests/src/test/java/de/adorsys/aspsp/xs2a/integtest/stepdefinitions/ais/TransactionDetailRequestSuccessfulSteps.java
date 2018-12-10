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
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Given;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.AccountDetails;
import de.adorsys.psd2.model.TransactionDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
@FeatureFileSteps
public class TransactionDetailRequestSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, TransactionDetails> context;

    @Autowired
    private ObjectMapper mapper;


    //@Given("^PSU already has an existing (.*) consent (.*)$")
    //in commonStep

   //@And("^account id (.*)$")
    //in AccountDetailRequestSuccessfulSteps



    @Given("^wants to read the transaction details using (.*)$")
    public void wants_to_read_the_transaction_details_using(String dataFileName) throws IOException {
        TestData<HashMap, TransactionDetails> data = mapper.readValue(
            resourceToString("/data-input/ais/transaction/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, TransactionDetails>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }

    @Given("^resource id (.*)$")
    public void resource_id(String resourceID) {
       context.setTransactionId(resourceID);
    }

    @When("^PSU requests the transaction details$")
    public void psu_requests_the_transaction_details() {
        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
            context.getAccessToken());
        ResponseEntity<TransactionDetails> response = restTemplate.exchange(
            context.getBaseUrl() + "/accounts/"+context.getRessourceId()+"/transactions/"+context.getTransactionId(),
            HttpMethod.GET,
            entity,
            TransactionDetails.class);

        log.info("////response///// "+response.toString());
        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate detail of transaction get returned$")
    public void a_successful_response_code_and_the_appropriate_detail_of_transaction_get_returned() throws Throwable {
        ResponseEntity<TransactionDetails> actualResponse = context.getActualResponse();
        TransactionDetails givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getTransactionId(), is(givenResponseBody.getTransactionId()));
    }


}

