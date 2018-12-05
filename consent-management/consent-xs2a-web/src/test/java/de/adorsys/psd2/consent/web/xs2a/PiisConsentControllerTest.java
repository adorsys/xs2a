/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.xs2a;

import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.web.xs2a.controller.PiisConsentController;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PiisConsentControllerTest {
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final String IBAN = "DE62500105179972514662";
    private static final String WRONG_IBAN = "FR7030066926176517166656113";

    @Mock
    private PiisConsentService piisConsentService;
    @InjectMocks
    private PiisConsentController piisConsentController;

    @Before
    public void setUp() {
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, AccountReferenceSelector.IBAN, IBAN))
            .thenReturn(Collections.singletonList(buildPiisConsent()));
        when(piisConsentService.getPiisConsentListByAccountIdentifier(CURRENCY, AccountReferenceSelector.IBAN, WRONG_IBAN))
            .thenReturn(Collections.emptyList());
    }

    @Test
    public void getPiisConsentById_Success() {
        // Given
        PiisConsent expected = buildPiisConsent();

        // When
        ResponseEntity<List<PiisConsent>> response = piisConsentController.getPiisConsentListByAccountReference("EUR", AccountReferenceSelector.IBAN, IBAN);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Collections.singletonList(expected));
    }

    @Test
    public void getPiisConsentById_Failure_WrongConsentId() {
        // When
        ResponseEntity<List<PiisConsent>> response = piisConsentController.getPiisConsentListByAccountReference("EUR", AccountReferenceSelector.IBAN, WRONG_IBAN);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setConsentStatus(ConsentStatus.VALID);
        return piisConsent;
    }
}
