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
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;

import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.ReadBalanceResponse200;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;


import static com.squareup.okhttp.internal.Util.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

@Slf4j
@FeatureFileSteps
public class ReadBalancesSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, ReadBalanceResponse200> context;

    @Autowired
    private ObjectMapper mapper;

    //@Given("^PSU already has an existing valid consent (.*)$")
    //in CommonSteps


    @And ("^And account id (.*) $")
    public void account_id(String accountId)  {
        context.setRessourceId(accountId);
    }



    @And ("^wants to read all balances using (.*)$")
    public void loadSuccessfulGetBalanceRequest (String dataFileName) throws IOException {

        TestData<HashMap, ReadBalanceResponse200> data = mapper.readValue(
            resourceToString("/data-input/ais/balance/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, ReadBalanceResponse200>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }

    @When("^PSU requests the balances$")
    public void request_the_balances () throws HttpClientErrorException {

        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
            context.getAccessToken());

        ResponseEntity<ReadBalanceResponse200> response = restTemplate.exchange(
            context.getBaseUrl() + "/accounts/" + context.getRessourceId() + "/balances",
            HttpMethod.GET,
            entity,
            ReadBalanceResponse200.class);

        context.setActualResponse(response);
        context.getTestData();

    }

    @Then ("^successful response code and the appropriate list of accounts get returned$")
    public void checkListOfAccounts () {

        ResponseEntity<ReadBalanceResponse200> actualResponse = context.getActualResponse();
        ReadBalanceResponse200 givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        assertThat(actualResponse.getBody().getBalances(), equalTo(givenResponseBody.getBalances()));
    }

}
