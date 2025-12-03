/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class VopXRequestIdHeaderValidatorImplTest {

    private VopXRequestIdHeaderValidatorImpl validator;

    @BeforeEach
    void setUp() {
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        validator = new VopXRequestIdHeaderValidatorImpl(errorBuildingServiceMock);
    }

    @Test
    void validate_validUuid() {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("X-Request-ID", "99391c7e-ad88-49ec-a2ad-99ddcb1f7721");
        
        MessageError messageError = new MessageError(ErrorType.AIS_400);
        
        MessageError result = validator.validate(headers, messageError);
        
        assertTrue(result.getTppMessages().isEmpty());
    }

    @Test
    void validate_invalidUuid() {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("X-Request-ID", "invalid-uuid");
        
        MessageError messageError = new MessageError(ErrorType.AIS_400);
        
        MessageError result = validator.validate(headers, messageError);
        
        assertTrue(result.getTppMessage() != null);
    }

    @Test
    void validate_missingHeader() {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        
        MessageError messageError = new MessageError(ErrorType.AIS_400);
        
        MessageError result = validator.validate(headers, messageError);
        
        assertTrue(result.getTppMessage() != null);
    }

    @Test
    void validate_nullHeader() {
        Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.put("X-Request-ID", null);
        
        MessageError messageError = new MessageError(ErrorType.AIS_400);
        
        MessageError result = validator.validate(headers, messageError);
        
        assertTrue(result.getTppMessage() != null);
    }
}