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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentTest {
    private static final PsuData PSU_DATA = new PsuData("psu", null, null, null);
    private static final TppInfoEntity TPP_INFO = new TppInfoEntity();

    @Test
    public void isWrongConsentData_shouldReturnTrue_emptyPsuDataList() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.emptyList(), TPP_INFO);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    public void isWrongConsentData_shouldReturnTrue_firstPsuIsNull() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(null), TPP_INFO);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    public void isWrongConsentData_shouldReturnTrue_tppInfoNull() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), null);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertTrue(actual);
    }

    @Test
    public void isWrongConsentData_shouldReturnFalse() {
        // Given
        AisConsent aisConsent = buildAisConsent(Collections.singletonList(PSU_DATA), TPP_INFO);

        // When
        boolean actual = aisConsent.isWrongConsentData();

        // Then
        assertFalse(actual);
    }

    private AisConsent buildAisConsent(List<PsuData> psuDataList, TppInfoEntity tppInfoEntity) {
        AisConsent consent = new AisConsent();
        consent.setPsuDataList(psuDataList);
        consent.setTppInfo(tppInfoEntity);
        return consent;
    }
}
