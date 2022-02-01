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

package de.adorsys.psd2.xs2a.web.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class LogbackPatternLayoutTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final LogbackPatternLayout layout = spy(new LogbackPatternLayout());
    private final String mask = LogbackPatternLayout.MASK;
    private final LoggerContext loggerContext = new LoggerContext();
    private final Logger logger = loggerContext.getLogger(Logger.FQCN);

    @Test
    void testFieldsAndObjectsMask() throws IOException {
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
        Map<String, Object> map = mapper.readValue(testMapRepresentationModified, new TypeReference<HashMap<String, Object>>() {
        });
        map.forEach((key, value) -> assertEquals(mask, value));
    }

    @Test
    void testAuthorizationHeaderMask() {
        //Given
        String authorization = "authorization: ";
        String bearer = "Bearer 1234567";
        //When
        String headerModified = layout.modifyMessage(authorization + bearer);
        //Then
        assertEquals(authorization + mask, headerModified);
    }

    @Test
    void testFieldsAndObjectsNoMask() throws IOException {
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
        Map<String, Object> map = mapper.readValue(testMapRepresentationModified, new TypeReference<HashMap<String, Object>>() {
        });
        map.forEach((key, value) -> assertNotSame(mask, value));
    }

    @Test
    void doLayout() {
        //When
        layout.doLayout(getEventObject());

        //Then
        verify(layout).modifyMessage("");
    }

    private ILoggingEvent getEventObject() {
        return new LoggingEvent(logger.getName(),
                                logger, Level.INFO, "Custom message", new Exception("Custom exception"), null);
    }
}
