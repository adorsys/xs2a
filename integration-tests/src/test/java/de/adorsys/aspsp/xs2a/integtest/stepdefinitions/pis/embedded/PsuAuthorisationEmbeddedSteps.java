///*
// * Copyright 2018-2018 adorsys GmbH & Co KG
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis.embedded;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import cucumber.api.java.en.And;
//import de.adorsys.aspsp.xs2a.integtest.model.TestData;
//import de.adorsys.aspsp.xs2a.integtest.util.Context;
//import de.adorsys.aspsp.xs2a.integtest.util.PaymentUtils;
//import de.adorsys.psd2.model.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.IOException;
//import java.util.HashMap;
//
//import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.apache.commons.io.IOUtils.resourceToString;
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.*;
//
//public class PsuAuthorisationEmbeddedSteps {
//
//    @Autowired
//    @Qualifier("xs2a")
//    private RestTemplate restTemplate;
//
//    @Autowired
//    private Context<HashMap, StartScaprocessResponse> context;
//
//    @Autowired
//    private ObjectMapper mapper;
//
//    @And("^PSU needs to authorize and identify using (.*)$")
//    public void psuAuthorizationUsingAuthorisationData(String paymentId, String dataFileName, String authorisationId) throws IOException {
//        context.setAuthorisationId(authorisationId);
//        TestData<HashMap, LinksPaymentInitiation> data = mapper.readValue(resourceToString(
//            "/data-input/pis/embedded/" + dataFileName, UTF_8),
//            new TypeReference<TestData<HashMap, TppMessages>>() {
//            });
//
//        HttpEntity entity = PaymentUtils.getHttpEntity(data.getRequest(), context.getAccessToken());
//
//        ResponseEntity<StartScaprocessResponse> scaProcessResponseEntity = restTemplate.exchange(
//            context.getBaseUrl() + "/" + paymentId + "/authorisations" + authorisationId,
//            HttpMethod.PUT,
//            entity,
//            StartScaprocessResponse.class);
//        context.setActualResponse(scaProcessResponseEntity);
//    }
//
//    @And("^check SCA methods$")
//    public void checkSCAMethods() {
//        ResponseEntity<StartScaprocessResponse> actualResponse = context.getActualResponse();
//        assertThat(actualResponse.getStatusCode(), equalTo(HttpStatus.OK));
//        assertThat(actualResponse.getBody().getScaStatus().toString(), equalTo(ScaStatus.PSUAUTHENTICATED.toString()));
//        assertThat(actualResponse.getBody().getScaMethods(), is(not(empty())));
//    }
//}
