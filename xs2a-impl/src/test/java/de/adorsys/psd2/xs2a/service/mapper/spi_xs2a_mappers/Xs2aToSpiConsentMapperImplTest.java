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

import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAisConsentRequestType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiBookingStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class Xs2aToSpiConsentMapperImplTest {
    private final Xs2aToSpiConsentMapper mapper = new Xs2aToSpiConsentMapperImpl();

    @Test
    void mapToSpiConsentStatus_null() {
        SpiConsentStatus actual = mapper.mapToSpiConsentStatus(null);
        assertNull(actual);
    }

    @Test
    void mapToSpiAisConsentRequestType_null() {
        SpiAisConsentRequestType actual = mapper.mapToSpiAisConsentRequestType(null);
        assertNull(actual);
    }

    @Test
    void mapToSpiConsentType_null() {
        SpiConsentType actual = mapper.mapToSpiConsentType(null);
        assertNull(actual);
    }

    @Test
    void mapToSpiBookingStatus_null() {
        SpiBookingStatus actual = mapper.mapToSpiBookingStatus(null);
        assertNull(actual);
    }

    @ParameterizedTest
    @EnumSource(ConsentStatus.class)
    void mapToSpiConsentStatus(ConsentStatus consentStatus) {
        SpiConsentStatus actual = mapper.mapToSpiConsentStatus(consentStatus);
        assertEquals(actual.name(), consentStatus.name());
    }

    @ParameterizedTest
    @EnumSource(AisConsentRequestType.class)
    void mapToSpiAisConsentRequestType(AisConsentRequestType consentRequestType) {
        SpiAisConsentRequestType actual = mapper.mapToSpiAisConsentRequestType(consentRequestType);
        assertEquals(actual.name(), consentRequestType.name());
    }

    @ParameterizedTest
    @EnumSource(ConsentType.class)
    void mapToSpiConsentType(ConsentType consentType) {
        SpiConsentType actual = mapper.mapToSpiConsentType(consentType);
        assertEquals(actual.name(), consentType.name());
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void mapToSpiBookingStatus(BookingStatus bookingStatus) {
        SpiBookingStatus actual = mapper.mapToSpiBookingStatus(bookingStatus);
        assertEquals(actual.name(), bookingStatus.name());
    }
}
