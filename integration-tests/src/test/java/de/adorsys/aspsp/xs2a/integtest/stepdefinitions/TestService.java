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

package de.adorsys.aspsp.xs2a.integtest.stepdefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.aspsp.xs2a.integtest.util.HttpEntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.resourceToString;

@Service
public class TestService {

    @Autowired
    @Qualifier("xs2a")
    private RestTemplate restTemplate;

    @Autowired
    private Context context;

    @Autowired
    private ObjectMapper mapper;

    public void sendRestCall (HttpMethod httpMethod, String url) {
        HttpEntity entity = HttpEntityUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        ResponseEntity<?> response = restTemplate.exchange(
            url,
            httpMethod,
            entity,
            context.getTestData().getResponse().getBody().getClass());

        context.setActualResponse(response);
    }

    public void sendRestCall(HttpMethod httpMethod, String url, HttpEntity entity) {
        ResponseEntity<?> response = restTemplate.exchange(
            url,
            httpMethod,
            entity,
            context.getTestData().getResponse().getBody().getClass());

        context.setActualResponse(response);
    }

    public void sendErrorfulRestCall (HttpMethod httpMethod, String url) throws IOException {
        HttpEntity entity = HttpEntityUtils.getHttpEntity(
            context.getTestData().getRequest(), context.getAccessToken());

        try {
            restTemplate.exchange(
                url,
                httpMethod,
                entity,
                context.getTestData().getResponse().getBody().getClass());
        } catch (RestClientResponseException rex) {
            context.handleRequestError(rex);
        }
    }

    public void sendErrorfulRestCall (HttpMethod httpMethod, String url, HttpEntity httpEntity) throws IOException {

        try {
            restTemplate.exchange(
                url,
                httpMethod,
                httpEntity,
                context.getTestData().getResponse().getBody().getClass());
        } catch (RestClientResponseException rex) {
            context.handleRequestError(rex);
        }
    }

    public void parseJson (String fileName, TypeReference typeReference) throws IOException {
        TestData<?, ?> data = mapper.readValue(resourceToString(
            fileName, UTF_8),
            typeReference);
        context.setTestData(data);
    }


}
