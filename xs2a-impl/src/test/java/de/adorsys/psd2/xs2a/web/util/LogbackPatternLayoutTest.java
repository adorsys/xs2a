/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.TestCase.assertEquals;

public class LogbackPatternLayoutTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final LogbackPatternLayout layout = new LogbackPatternLayout();
    private final String mask = LogbackPatternLayout.MASK;

    @Test
    public void testFieldsAndObjectsMask() throws IOException {
        //Given
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("password", "password");
        testMap.put("encryptedPassword", "encryptedPassword");
        testMap.put("additionalPassword", "additionalPassword");
        testMap.put("additionalEncryptedPassword", "additionalEncryptedPassword");
        testMap.put("access_token", "access_token");
        testMap.put("refresh_token", "refresh_token");
        testMap.put("ownerName", "ownerName");
        Xs2aAddress address = new Xs2aAddress();
        address.setTownName("town");
        testMap.put("ownerAddress", address);

        String testMapRepresentation = mapper.writeValueAsString(testMap);
        //When
        String testMapRepresentationModified = layout.modifyMessage(testMapRepresentation);
        //Then
        Map<String, Object> map = mapper.readValue(testMapRepresentationModified, new TypeReference<HashMap<String,Object>>() {});
        map.forEach((key, value) -> assertEquals(mask, value));
    }

    @Test
    public void testAuthorizationHeaderMask() {
        //Given
        String authorization = "authorization: ";
        String bearer = "Bearer 1234567";
        //When
        String headerModified = layout.modifyMessage(authorization + bearer);
        //Then
        assertEquals(authorization + mask, headerModified);
    }

    @Test
    public void testFieldsAndObjectsNoMask() throws IOException {
        //Given
        Map<String, Object> testMap = new HashMap<>();
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("IBAN");
        List<AccountReference> accountReferenceList = Collections.singletonList(accountReference);
        testMap.put("ownerName", accountReferenceList);
        testMap.put("ownerAddress", accountReferenceList);

        String testMapRepresentation = mapper.writeValueAsString(testMap);
        //When
        String testMapRepresentationModified = layout.modifyMessage(testMapRepresentation);
        //Then
        Map<String, Object> map = mapper.readValue(testMapRepresentationModified, new TypeReference<HashMap<String,Object>>() {});
        map.forEach((key, value) -> assertNotSame(mask, value));
    }
}
