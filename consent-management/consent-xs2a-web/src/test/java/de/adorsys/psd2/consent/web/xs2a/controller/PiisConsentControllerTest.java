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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PiisConsentControllerTest {
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String IBAN = "DE62500105179972514662";
    private static final String WRONG_IBAN = "FR7030066926176517166656113";

    @Mock
    private PiisConsentService piisConsentService;
    @InjectMocks
    private PiisConsentController piisConsentController;

    @Test
    void getPiisConsentById_Success() {
        // Given
        PiisConsent expected = buildPiisConsent();
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, new AccountReferenceSelector(AccountReferenceType.IBAN, IBAN)))
            .thenReturn(Collections.singletonList(buildPiisConsent()));

        // When
        ResponseEntity<List<PiisConsent>> response = piisConsentController.getPiisConsentListByAccountReference("EUR", AccountReferenceType.IBAN, IBAN);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonList(expected), response.getBody());
    }

    @Test
    void getPiisConsentById_Failure_WrongConsentId() {
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, new AccountReferenceSelector(AccountReferenceType.IBAN, WRONG_IBAN)))
            .thenReturn(Collections.emptyList());
        // When
        ResponseEntity<List<PiisConsent>> response = piisConsentController.getPiisConsentListByAccountReference("EUR", AccountReferenceType.IBAN, WRONG_IBAN);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        return piisConsent;
    }
}
