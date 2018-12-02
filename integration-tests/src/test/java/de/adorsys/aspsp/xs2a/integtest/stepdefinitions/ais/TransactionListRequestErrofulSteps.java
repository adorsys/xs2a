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
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.TppMessages;
import de.adorsys.psd2.model.TransactionsResponse200Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
@FeatureFileSteps
public class TransactionListRequestErrofulSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;



    //@Given("^PSU already has an existing (.*) consent (.*)$")
    //in commonStep

    //@And("^account id (.*)$")
    //in AccountDetailRequestSuccessfulSteps


    @Given("^wants to read all transactions errorful using (.*)$")
    public void wants_to_read_all_transactions_erroful_using(String dataFileName) throws IOException {
        TestData<HashMap, TppMessages> data = mapper.readValue(
            resourceToString("/data-input/ais/transaction/" + dataFileName, UTF_8),
            new TypeReference<TestData<HashMap, TppMessages>>() {});

        context.setTestData(data);
        context.getTestData().getRequest().getHeader().put("Consent-ID", context.getConsentId());
    }


    @When("^PSU requests the transactions erroful$")
    public void psu_requests_the_transactions() throws HttpClientErrorException, IOException{
        HttpEntity entity = HttpEntityUtils.getHttpEntity(context.getTestData().getRequest(),
                context.getAccessToken());
        String url = context.getBaseUrl() + "/accounts/"+context.getRessourceId()+"/transactions";
        log.info("////url////  "+url);
        log.info("////entity request list transaction////  "+entity.toString());
        try {
                ResponseEntity<TransactionsResponse200Json> response = restTemplate.exchange(
                        context.getBaseUrl() + "/accounts/"+context.getRessourceId()+"/transactions/?dateFrom=2008-09-15?bookingStatus=both",
                        HttpMethod.GET,
                        entity,
                        TransactionsResponse200Json.class);
        } catch (RestClientResponseException rex) {
            context.handleRequestError(rex);
        }
    }

    //@Then("^an error response code is displayed and an appropriate error response is shown$")
    //See commonStep
}

