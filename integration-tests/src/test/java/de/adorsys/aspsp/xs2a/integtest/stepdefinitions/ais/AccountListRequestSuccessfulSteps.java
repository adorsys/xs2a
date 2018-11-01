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
import de.adorsys.psd2.model.AccountList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@FeatureFileSteps
public class AccountListRequestSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, AccountList> context;

    @Autowired
    private ObjectMapper mapper;




    @And("^wants to get a list of accounts using (.*)$")
    public void wants_to_get_a_list_of_accounts_using(String dataFileName) throws IOException {
        TestData<HashMap, AccountList> data = mapper.readValue(
                resourceToString("/data-input/ais/account/" + dataFileName, UTF_8),
                new TypeReference<TestData<HashMap, AccountList>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }

    @When("^PSU requests the list of accounts$")
    public void psu_requests_the_list_of_accounts() {
        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
                context.getAccessToken());
        log.info("////entity request list account////  "+entity.toString());
        ResponseEntity<AccountList> response = restTemplate.exchange(
                context.getBaseUrl() + "/accounts",
                HttpMethod.GET,
                entity,
                AccountList.class);

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate list of accounts get returned$")
    public void a_successful_response_code_and_the_appropriate_list_of_accounts_get_returned() {
        ResponseEntity<AccountList> actualResponse = context.getActualResponse();
        AccountList givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
        //TODO assert that the response body is what we expect
        //we expect at least one accountDetail in the list
    }

}

