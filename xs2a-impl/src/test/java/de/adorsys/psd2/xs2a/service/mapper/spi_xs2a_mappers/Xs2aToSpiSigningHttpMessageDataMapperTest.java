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

import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import de.adorsys.psd2.xs2a.spi.domain.vop.security.SpiSigningHttpMessageData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Xs2aToSpiSigningHttpMessageDataMapperTest {
    private static final String DIGEST = "SHA-256=X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=";
    private static final String X_JWS_SIGNATURE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

    private final Xs2aToSpiSigningHttpMessageDataMapper mapper = new Xs2aToSpiSigningHttpMessageDataMapper();

    @Test
    void mapToSpiSigningHttpMessageData_WithAllFields() {
        // Given
        SigningHttpMessage signingHttpMessage = new SigningHttpMessage(
            Optional.of(DIGEST),
            Optional.of(X_JWS_SIGNATURE)
        );
        
        // When
        SpiSigningHttpMessageData actual = mapper.mapToSpiSigningHttpMessageData(signingHttpMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.of(DIGEST), actual.getDigest());
        assertEquals(Optional.of(X_JWS_SIGNATURE), actual.getXJwsSignature());
    }

    @Test
    void mapToSpiSigningHttpMessageData_WithEmptyOptionals() {
        // Given
        SigningHttpMessage signingHttpMessage = new SigningHttpMessage(
            Optional.empty(),
            Optional.empty()
        );
        
        // When
        SpiSigningHttpMessageData actual = mapper.mapToSpiSigningHttpMessageData(signingHttpMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.empty(), actual.getDigest());
        assertEquals(Optional.empty(), actual.getXJwsSignature());
    }

    @Test
    void mapToSpiSigningHttpMessageData_WithOnlyDigest() {
        // Given
        SigningHttpMessage signingHttpMessage = new SigningHttpMessage(
            Optional.of(DIGEST),
            Optional.empty()
        );
        
        // When
        SpiSigningHttpMessageData actual = mapper.mapToSpiSigningHttpMessageData(signingHttpMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.of(DIGEST), actual.getDigest());
        assertEquals(Optional.empty(), actual.getXJwsSignature());
    }

    @Test
    void mapToSpiSigningHttpMessageData_WithOnlySignature() {
        // Given
        SigningHttpMessage signingHttpMessage = new SigningHttpMessage(
            Optional.empty(),
            Optional.of(X_JWS_SIGNATURE)
        );
        
        // When
        SpiSigningHttpMessageData actual = mapper.mapToSpiSigningHttpMessageData(signingHttpMessage);
        
        // Then
        assertNotNull(actual);
        assertEquals(Optional.empty(), actual.getDigest());
        assertEquals(Optional.of(X_JWS_SIGNATURE), actual.getXJwsSignature());
    }

    @Test
    void mapToSpiSigningHttpMessageData_WithNullInput() {
        // When
        SpiSigningHttpMessageData actual = mapper.mapToSpiSigningHttpMessageData(null);
        
        // Then
        assertNotNull(actual);
        assertNull(actual.getDigest());
        assertNull(actual.getXJwsSignature());
    }
}
