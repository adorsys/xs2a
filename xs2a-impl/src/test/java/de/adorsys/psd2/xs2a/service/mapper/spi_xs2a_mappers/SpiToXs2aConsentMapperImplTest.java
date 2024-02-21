/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAisConsentRequestType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpiToXs2aConsentMapperImplTest {
    private final SpiToXs2aConsentMapperImpl mapper = new SpiToXs2aConsentMapperImpl();

    @Test
    void mapToConsentStatus_null() {
        ConsentStatus actual = mapper.mapToConsentStatus(null);
        assertNull(actual);
    }

    @Test
    void mapToAisConsentRequestType_null() {
        AisConsentRequestType actual = mapper.mapToAisConsentRequestType(null);
        assertNull(actual);
    }

    @Test
    void mapToConsentType_null() {
        ConsentType actual = mapper.mapToConsentType(null);
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(SpiConsentStatus.class)
    void mapToConsentStatus(SpiConsentStatus spiConsentStatus) {
        ConsentStatus actual = mapper.mapToConsentStatus(spiConsentStatus);
        assertEquals(actual.name(), spiConsentStatus.name());
    }

    @ParameterizedTest
    @EnumSource(SpiAisConsentRequestType.class)
    void mapToAisConsentRequestType(SpiAisConsentRequestType consentRequestType) {
        AisConsentRequestType actual = mapper.mapToAisConsentRequestType(consentRequestType);
        assertEquals(actual.name(), consentRequestType.name());
    }

    @ParameterizedTest
    @EnumSource(SpiConsentType.class)
    void mapToConsentType(SpiConsentType consentType) {
        ConsentType actual = mapper.mapToConsentType(consentType);
        assertEquals(actual.name(), consentType.name());
    }
}
