/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
