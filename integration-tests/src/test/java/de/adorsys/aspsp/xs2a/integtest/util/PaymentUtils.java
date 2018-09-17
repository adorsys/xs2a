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

package de.adorsys.aspsp.xs2a.integtest.util;

import de.adorsys.aspsp.xs2a.integtest.model.Request;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class PaymentUtils {

    public static HttpEntity getHttpEntity(Request request, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (request != null)
            headers.setAll(request.getHeader());
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return new HttpEntity<>(request != null ? request.getBody() : null, headers);
    }

    public static HttpEntity getHttpEntity(String scaInformation, Context context) {
        MultiValueMap<String, String> bodyWithScaData = new LinkedMultiValueMap<>();
        // we need to setup sca info from data file or from other source
        if (scaInformation.contains("Sca-Method")) {
            bodyWithScaData.add(scaInformation, context.getScaMethod());
        } else {
            bodyWithScaData.add(scaInformation, context.getTanValue());
        }
        return new HttpEntity<>(bodyWithScaData, getHeaders(context));
    }

    private static HttpHeaders getHeaders(Context context) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + context.getAccessToken());
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return headers;
    }
}
