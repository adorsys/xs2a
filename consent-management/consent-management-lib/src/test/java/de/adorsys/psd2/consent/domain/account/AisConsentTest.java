/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AisConsentTest {
    private static final PsuData PSU_DATA = new PsuData("psu", null, null, null, null);
    private static final TppInfoEntity TPP_INFO = new TppInfoEntity();

    @Test
    void isWrongConsentData_shouldReturnTrue_emptyPsuDataList() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.emptyList(), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    void isWrongConsentData_shouldReturnTrue_tppInfoNull() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), null, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    void isWrongConsentData_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnFalse_recurringConsent() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, true);

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnFalse_nonRecurringWithoutOldUsages() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
        aisConsent.setUsages(Collections.singletonList(buildAisConsentUsage(LocalDate.now())));

        // When
        boolean actual = aisConsent.isNonReccuringAlreadyUsed();

        // Then
        assertFalse(actual);
    }

    @Test
    void isNonReccuringAlreadyUsed_shouldReturnTrue_nonRecurringWithOldUsages() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO, false);
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
        AisConsent aisConsent = new AisConsent();
        String expectedInternalRequestId = "internal-request-id";
        aisConsent.setInternalRequestId(expectedInternalRequestId);

        // When
        String actualInternalRequestId = aisConsent.getInternalRequestId(AuthorisationType.AIS);

        // Then
        assertEquals(expectedInternalRequestId, actualInternalRequestId);
    }

    @Test
    void getInternalRequestId_PisAuthorisation_shouldThrowException() {
        // Given
        AisConsent aisConsent = new AisConsent();

        // Then
        assertThrows(IllegalArgumentException.class,
                     () -> aisConsent.getInternalRequestId(AuthorisationType.PIS_CREATION));
    }

    private AisConsent buildAisConsent(List<PsuData> psuDataList, TppInfoEntity tppInfoEntity, boolean recurringIndicator) {
        AisConsent consent = new AisConsent();
        consent.setPsuDataList(psuDataList);
        consent.setTppInfo(tppInfoEntity);
        consent.setRecurringIndicator(recurringIndicator);
        return consent;
    }

    private AisConsentUsage buildAisConsentUsage(LocalDate usageDate) {
        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(usageDate);
        return usage;
    }
}
