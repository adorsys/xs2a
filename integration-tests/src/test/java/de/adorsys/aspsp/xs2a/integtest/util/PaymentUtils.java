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

public class PaymentUtils {

    public static HttpEntity getHttpEntity(Request request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(request.getHeader());
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");

        return new HttpEntity<>(request.getBody(), headers);
    }
}
