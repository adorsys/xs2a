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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.psd2.model.LinksSelectPsuAuthenticationMethod;
import de.adorsys.psd2.model.ScaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@FeatureFileSteps
public class PsuAuthorisationWithScaMethodEmbeddedSteps {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<HashMap, LinksSelectPsuAuthenticationMethod> context;

    @When("^PSU sends the authorisation request with the payment-id (.*) and authorisationId (.*)$")
    public void psuSendsTheAuthorisationRequest(String paymentId, String authorisationId) {
        context.setScaMethod(SpiScaMethod.SMS_OTP.toString());
        HttpEntity entityWithScaMethod = PaymentUtils.getHttpEntityWithScaInformation("Sca-Method", context);

        ResponseEntity<LinksSelectPsuAuthenticationMethod> authenticationMethodResponseEntity = restTemplate.exchange(
            context.getBaseUrl() + "/" + paymentId + "/authorisations" + authorisationId,
            HttpMethod.PUT,
            entityWithScaMethod,
            LinksSelectPsuAuthenticationMethod.class);

        context.setActualResponse(authenticationMethodResponseEntity);
    }

    @And("^check SCA status$")
    public void checkSCAStatus() {
        ResponseEntity<LinksSelectPsuAuthenticationMethod> actualResponse = context.getActualResponse();
        assertThat(actualResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(actualResponse.getBody().getScaStatus(), equalTo(ScaStatus.SCAMETHODSELECTED.toString()));
    }
}
