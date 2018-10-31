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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.TestService;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.psd2.model.*;
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

public class SuccessfulRequestForAuthorisationsIDs {


    @Autowired
    private Context context;

    @Autowired
    private TestService testService;


    //    @Given("^PSU sends the single payment initiation request and receives the paymentId$")
    //    See Global Successful Steps

    // @And("^PSU sends the start authorisation request and receives the authorisationId$")
    // See GlobalSuccessfulSteps


    @When("^PSU sends the successful authorisation IDs data request$")
    public void sendGetAuthorisationIdsRequest() throws IOException{

        /*TestData<HashMap, Authorisations> data = mapper.readValue(resourceToString(
            "/data-input/pis/embedded/" + "RequestAuthorisationIDs-successful.json", UTF_8),
            new TypeReference<TestData<HashMap, Authorisations>>() {
            });
        context.setTestData(data);*/

        testService.parseJson("/data-input/pis/embedded/RequestAuthorisationIDs-successful.json",
            new TypeReference<TestData<HashMap, Authorisations>>(){});


      /*  HttpEntity entity = PaymentUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<Authorisations> response = restTemplate.exchange(
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/",
            HttpMethod.GET,
            entity,
            Authorisations.class);

        context.setActualResponse(response);*/

        testService.sendRestCall(HttpMethod.GET,
            context.getBaseUrl() + "/" + context.getPaymentService() + "/" + context.getPaymentId() + "/authorisations/");
    }

    @Then("^a successful response code and the appropriate list of authorisation Ids are received$")
    public void aSuccessfulResponseCodeAndTheAppropriateListOfAuthorisationIdsAreReceived()throws Exception{
        ResponseEntity<Authorisations> actualResponse = context.getActualResponse();
        Authorisations givenResponse= setIdIntoList(context.getAuthorisationId());

        assertThat(actualResponse.getBody().getAuthorisationIds(), equalTo(givenResponse.getAuthorisationIds()));
        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));
    }

    private Authorisations setIdIntoList(String authorisationId) throws Exception {

        AuthorisationsList idList= new AuthorisationsList();
        boolean add=idList.add(authorisationId);
        Authorisations res= new Authorisations();
        if(add){
            res.setAuthorisationIds(idList);
            return res;
        }else throw new Exception("No authorisationID found");
    }
}
