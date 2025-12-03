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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSecuringBodyMessageData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Xs2aToSpiSecuringBodyMessageDataMapperTest {
    private static final String BODY_SIG_PROFILE = "http://uri.etsi.org/19182/HttpHeaders#";
    private static final String BODY_ENC_PROFILE = "http://uri.etsi.org/19182/HttpHeaders#encryption";
    private static final String BODY_ENC_LIST = "AES256";

    private final Xs2aToSpiSecuringBodyMessageDataMapper mapper = new Xs2aToSpiSecuringBodyMessageDataMapper();

    @Test
    void mapToSpiSecuringBodyMessageData_WithAllFields() {
        // Given
        SecuringBodyMessage securingBodyMessage = new SecuringBodyMessage(
            Optional.of(BODY_SIG_PROFILE),
            Optional.of(BODY_ENC_PROFILE),
            Optional.of(BODY_ENC_LIST)
        );
        
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(securingBodyMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.of(BODY_SIG_PROFILE), actual.getBodySigProfile());
        assertEquals(Optional.of(BODY_ENC_PROFILE), actual.getBodyEncProfile());
        assertEquals(Optional.of(BODY_ENC_LIST), actual.getBodyEncList());
    }

    @Test
    void mapToSpiSecuringBodyMessageData_WithEmptyOptionals() {
        // Given
        SecuringBodyMessage securingBodyMessage = new SecuringBodyMessage(
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );
        
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(securingBodyMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.empty(), actual.getBodySigProfile());
        assertEquals(Optional.empty(), actual.getBodyEncProfile());
        assertEquals(Optional.empty(), actual.getBodyEncList());
    }

    @Test
    void mapToSpiSecuringBodyMessageData_WithOnlySignatureProfile() {
        // Given
        SecuringBodyMessage securingBodyMessage = new SecuringBodyMessage(
            Optional.of(BODY_SIG_PROFILE),
            Optional.empty(),
            Optional.empty()
        );
        
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(securingBodyMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.of(BODY_SIG_PROFILE), actual.getBodySigProfile());
        assertEquals(Optional.empty(), actual.getBodyEncProfile());
        assertEquals(Optional.empty(), actual.getBodyEncList());
    }

    @Test
    void mapToSpiSecuringBodyMessageData_WithOnlyEncryptionProfile() {
        // Given
        SecuringBodyMessage securingBodyMessage = new SecuringBodyMessage(
            Optional.empty(),
            Optional.of(BODY_ENC_PROFILE),
            Optional.empty()
        );
        
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(securingBodyMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.empty(), actual.getBodySigProfile());
        assertEquals(Optional.of(BODY_ENC_PROFILE), actual.getBodyEncProfile());
        assertEquals(Optional.empty(), actual.getBodyEncList());
    }

    @Test
    void mapToSpiSecuringBodyMessageData_WithOnlyEncryptionList() {
        // Given
        SecuringBodyMessage securingBodyMessage = new SecuringBodyMessage(
            Optional.empty(),
            Optional.empty(),
            Optional.of(BODY_ENC_LIST)
        );
        
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(securingBodyMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.empty(), actual.getBodySigProfile());
        assertEquals(Optional.empty(), actual.getBodyEncProfile());
        assertEquals(Optional.of(BODY_ENC_LIST), actual.getBodyEncList());
    }

    @Test
    void mapToSpiSecuringBodyMessageData_WithNullInput() {
        // When
        SpiSecuringBodyMessageData actual = mapper.mapToSpiSecuringBodyMessageData(null);
        
        // Then
        assertNotNull(actual);
        assertNull(actual.getBodySigProfile());
        assertNull(actual.getBodyEncProfile());
        assertNull(actual.getBodyEncList());
    }
}
