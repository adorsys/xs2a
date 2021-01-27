/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.validator.util;

import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DigestSignatureHelperTest {

    @Test
    void testDigestAndSignatureHeadersCreation() throws Exception {
        DigestSignatureHelper digestSignatureHelper = new DigestSignatureHelper();
        JsonReader jsonReader = new JsonReader();

        // Create digest based on request payload
        String digest = digestSignatureHelper.digest(jsonReader.getStringFromFile("helper/request-body.txt").trim());
        System.out.println("DIGEST = " + digest);

        // Request header map which are used to compose content for signing
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("accept", "application/json");
        headerMap.put("psu-ip-address", "1.1.1.1");
        headerMap.put("psu-id", "anton.brueckner");
        headerMap.put("x-request-id", "2f77a125-aa7a-45c0-b414-cea25a116035");
        headerMap.put("digest", digest);

        // Signing content and sequence of headers
        String content = headerMap.keySet().stream()
                             .map(key -> key + ": " + headerMap.get(key))
                             .collect(Collectors.joining("\n"));
        System.out.println("CONTENT = " + content);
        String headers = String.join(" ", headerMap.keySet());
        System.out.println("HEADERS = " + headers);

        // `signature` header that contains keyId - certificate info, headers - ordered lowercase headers, algorithm, signature
        String signature = digestSignatureHelper.sign(content);
        String signatureHeader = digestSignatureHelper.getSignatureHeader(signature, headers);
        System.out.println("signature = " + signatureHeader);

        // verify signature
        boolean verify = digestSignatureHelper.verify(content, signature);
        assertTrue(verify);
    }
}
