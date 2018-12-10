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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.ais.embedded;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.FeatureFileSteps;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.model.ConsentsResponse201;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@FeatureFileSteps
public class ConsentRequestImpliciteStartAuthorizationSuccessfulSteps {

    @Autowired
    @Qualifier("aspsp-profile")
    private RestTemplate restTemplate;

    @Autowired
    private Context<Consents, ConsentsResponse201> context;

    @And("^response contains link startAuthorisationWIthPsuAuthentication$")
    public void checkStartAuthorisationWIthPsuAuthentication() {
        ResponseEntity<ConsentsResponse201> actualResponse = context.getActualResponse();
        assertThat(actualResponse.getBody().getLinks().get("startAuthorisationWIthPsuAuthentication$"), notNullValue());
    }

    //@After
    public void resetSigningBasketSupportedProfile(){
        HttpEntity entity = HttpEntityUtils.getHttpEntity(Boolean.FALSE);
        this.restTemplate.put(context.getProfileUrl() + "/aspsp-profile/for-debug/signing-basket-supported", entity);


    }
}
