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

import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.api.java.en.When;
import de.adorsys.aspsp.xs2a.integtest.entities.ITMessageError;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.BulkPaymentInitiationSctJson;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@FeatureFileSteps
public class BulkPaymentWithErrorSteps {
    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context<BulkPaymentInitiationSctJson, TppMessages> context;

    @Autowired
    private ObjectMapper mapper;

    @When("^PSU sends the bulk payment initiating request with error$")
    public void sendBulkPaymentInitiatingRequest() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(context.getTestData().getRequest().getHeader());
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");

        try {
            ResponseEntity<TppMessages> response = restTemplate.exchange(
                context.getBaseUrl() + "/bulk-payments/" + context.getPaymentProduct(),
                HttpMethod.POST, new HttpEntity<>(context.getTestData().getRequest().getBody(), headers), new ParameterizedTypeReference<TppMessages>() {
                });

            context.setActualResponse(response);
        } catch (HttpClientErrorException hce) {
            context.setActualResponseStatus(HttpStatus.valueOf(hce.getRawStatusCode()));

            ITMessageError messageError = mapper.readValue(hce.getResponseBodyAsString(), ITMessageError.class);
            context.setMessageError(messageError);
        }
    }
}
