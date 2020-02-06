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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AisAuthServiceTest {
    private static final String PARENT_ID = "consent ID";

    @InjectMocks
    private AisAuthService service;

    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    @Test
    void getInteractableAuthorisationParent_statusIsNotFinalized() {
        AisConsent consent = new AisConsent();
        consent.setConsentStatus(ConsentStatus.VALID);

        when(aisConsentJpaRepository.findByExternalId(PARENT_ID)).thenReturn(Optional.of(consent));

        assertEquals(Optional.of(consent), service.getNotFinalisedAuthorisationParent(PARENT_ID));

        verify(aisConsentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void getInteractableAuthorisationParent_statusIsFinalized() {
        AisConsent consent = new AisConsent();
        consent.setConsentStatus(ConsentStatus.EXPIRED);

        when(aisConsentJpaRepository.findByExternalId(PARENT_ID)).thenReturn(Optional.of(consent));

        assertEquals(Optional.empty(), service.getNotFinalisedAuthorisationParent(PARENT_ID));

        verify(aisConsentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void getAuthorisationParent() {
        AisConsent consent = new AisConsent();
        when(aisConsentJpaRepository.findByExternalId(PARENT_ID)).thenReturn(Optional.of(consent));

        assertEquals(Optional.of(consent), service.getAuthorisationParent(PARENT_ID));

        verify(aisConsentJpaRepository, times(1)).findByExternalId(PARENT_ID);
    }

    @Test
    void updateAuthorisable() {
        AisConsent consent = new AisConsent();

        service.updateAuthorisable(consent);
        verify(aisConsentJpaRepository, times(1)).save(consent);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration() {
        AisConsent initialConsent = new AisConsent();
        AisConsent updatedConsent = new AisConsent();
        when(aisConsentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(initialConsent)).thenReturn(updatedConsent);

        Authorisable response = service.checkAndUpdateOnConfirmationExpiration(initialConsent);

        assertEquals(updatedConsent, response);
        verify(aisConsentConfirmationExpirationService).checkAndUpdateOnConfirmationExpiration(initialConsent);
    }

    @Test
    void isConfirmationExpired() {
        AisConsent initialConsent = new AisConsent();
        when(aisConsentConfirmationExpirationService.isConfirmationExpired(initialConsent)).thenReturn(true);

        boolean response = service.isConfirmationExpired(initialConsent);

        assertTrue(response);
        verify(aisConsentConfirmationExpirationService).isConfirmationExpired(initialConsent);
    }

    @Test
    void updateOnConfirmationExpiration() {
        AisConsent initialConsent = new AisConsent();
        AisConsent updatedConsent = new AisConsent();
        when(aisConsentConfirmationExpirationService.updateOnConfirmationExpiration(initialConsent)).thenReturn(updatedConsent);

        Authorisable response = service.updateOnConfirmationExpiration(initialConsent);

        assertEquals(updatedConsent, response);
        verify(aisConsentConfirmationExpirationService).updateOnConfirmationExpiration(initialConsent);
    }

    @Test
    void getAuthorisationType() {
        assertEquals(AuthorisationType.AIS, service.getAuthorisationType());
    }
}
