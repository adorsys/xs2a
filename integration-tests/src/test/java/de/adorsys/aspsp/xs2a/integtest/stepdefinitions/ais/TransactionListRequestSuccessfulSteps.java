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
import de.adorsys.psd2.model.AccountList;
import de.adorsys.psd2.model.TransactionsResponse200Json;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import static org.hamcrest.Matchers.is;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@FeatureFileSteps
public class TransactionListRequestSuccessfulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, TransactionsResponse200Json> context;

    @Autowired
    private ObjectMapper mapper;



    //@Given("^PSU already has an existing (.*) consent (.*)$")
    //in commonStep

    //@And("^account id (.*)$")
    //in AccountDetailRequestSuccessfulSteps


    @Given("^balance (.*) dateFrom (.*) dateTo (.*) bookingStatus (.*) entryReferenceFrom (.*) deltaList (.*)$")
    public void setParameter(Boolean balance, String dateFrom, String dateTo, String bookingStatus, String entryReferenceFrom, Boolean deltaList) {

        String query = "?";

        if(BooleanUtils.isTrue(balance)){
            query = query+"balance=true&";
        }
        if(BooleanUtils.isFalse(balance)){
            query = query+"balance=false&";
        }
        if(StringUtils.isNotEmpty(dateFrom)){
            query = query+"dateFrom="+dateFrom+"&";
        }
        if(StringUtils.isNotEmpty(dateTo)){
            query = query+"dateTo="+dateTo+"&";
        }
        if(StringUtils.isNotEmpty(bookingStatus)){
            query = query+"bookingStatus="+bookingStatus+"&";
        }
        if(StringUtils.isNotEmpty(entryReferenceFrom)){
            query = query+"entryReferenceFrom="+entryReferenceFrom+"&";
        }
        if(BooleanUtils.isTrue(deltaList)){
            query = query+"deltaList=true";
        }
        if(BooleanUtils.isFalse(deltaList)){
            query = query+"deltaList=false";
        }

        context.setQueryParams(query);

    }

    @Given("^wants to read all transactions using (.*)$")
    public void wants_to_read_all_transactions_using(String dataFileName) throws IOException {
        TestData<HashMap, TransactionsResponse200Json> data = mapper.readValue(
                resourceToString("/data-input/ais/transaction/" + dataFileName, UTF_8),
                new TypeReference<TestData<HashMap, TransactionsResponse200Json>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }

    @When("^PSU requests the transactions$")
    public void psu_requests_the_transactions() {
        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
                context.getAccessToken());
        String url = context.getBaseUrl() + "/accounts/"+context.getRessourceId()+"/transactions/"+context.getQueryParams();
        log.info("////url////  "+url);
        log.info("////entity request list transaction////  "+entity.toString());
        ResponseEntity<TransactionsResponse200Json> response = restTemplate.exchange(
                context.getBaseUrl() + "/accounts/"+context.getRessourceId()+"/transactions/"+context.getQueryParams(),
                HttpMethod.GET,
                entity,
                TransactionsResponse200Json.class);

        context.setActualResponse(response);
    }

    @Then("^a successful response code and the appropriate list of transaction get returned$")
    public void a_successful_response_code_and_the_appropriate_list_of_transaction_get_returned(){
        ResponseEntity<TransactionsResponse200Json> actualResponse = context.getActualResponse();
        TransactionsResponse200Json givenResponseBody = context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
    }


}

