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

package de.adorsys.psd2.consent.domain.consent;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsentEntityTest {
    private static final PsuData PSU_DATA = new PsuData("psu", null, null, null, null);
    private static final TppInfoEntity TPP_INFO = new TppInfoEntity();

    @Test
    void isWrongConsentData_shouldReturnTrue_emptyPsuDataList() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.emptyList(), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    void isWrongConsentData_shouldReturnTrue_tppInfoNull() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.singletonList(PSU_DATA), null, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    void isWrongConsentData_shouldReturnFalse() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnFalse_recurringConsent() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.singletonList(PSU_DATA), TPP_INFO, true);

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnFalse_nonRecurringWithoutOldUsages() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
        aisConsent.setUsages(Collections.singletonList(buildAisConsentUsage(LocalDate.now())));

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnTrue_nonRecurringWithOldUsages() {
        // Given
        ConsentEntity aisConsent = buildConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
        aisConsent.setUsages(Arrays.asList(buildAisConsentUsage(LocalDate.now()),
                                           buildAisConsentUsage(LocalDate.now().minusDays(1))));

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertTrue(actual);
    }

    @Test
    void getInternalRequestId_AisAuthorisation() {
        // Given
        ConsentEntity aisConsent = new ConsentEntity();
        String expectedInternalRequestId = "internal-request-id";
        aisConsent.setInternalRequestId(expectedInternalRequestId);

        // When
        String actualInternalRequestId = aisConsent.getInternalRequestId(AuthorisationType.CONSENT);

        // Then
        assertEquals(expectedInternalRequestId, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_PisAuthorisation_shouldThrowException() {
        // Given
        ConsentEntity aisConsent = new ConsentEntity();

        // Then
        assertThrows(IllegalArgumentException.class,
                     () -> aisConsent.getInternalRequestId(AuthorisationType.PIS_CREATION));
    }

    @Test
    void shouldConsentBeExpired_PiisTppConsent() {
        //Given
        ConsentEntity piisConsent = new ConsentEntity();
        piisConsent.setConsentType(ConsentType.PIIS_TPP.getName());
        //When
        boolean shouldConsentBeExpired = piisConsent.shouldConsentBeExpired();
        //Then
        assertFalse(shouldConsentBeExpired);
    }

    private ConsentEntity buildConsent(List<PsuData> psuDataList, TppInfoEntity tppInfoEntity, boolean recurringIndicator) {
        ConsentEntity consent = new ConsentEntity();
        consent.setPsuDataList(psuDataList);
        ConsentTppInformationEntity consentTppInformation = new ConsentTppInformationEntity();
        consentTppInformation.setTppInfo(tppInfoEntity);
        consent.setTppInformation(consentTppInformation);
        consent.setRecurringIndicator(recurringIndicator);
        return consent;
    }

    private AisConsentUsage buildAisConsentUsage(LocalDate usageDate) {
        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(usageDate);
        return usage;
    }
}
