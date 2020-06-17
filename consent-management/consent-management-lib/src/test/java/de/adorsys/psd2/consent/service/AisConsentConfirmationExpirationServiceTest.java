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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.ais.ConsentTypeSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisConsentConfirmationExpirationServiceTest {
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate TODAY = LocalDate.now();

    @InjectMocks
    private AisConsentConfirmationExpirationServiceImpl expirationService;

    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AspspProfileService aspspProfileService;

    @Test
    void expireConsent() {
        // Given
        ArgumentCaptor<ConsentEntity> aisConsentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);
        // When
        expirationService.expireConsent(new ConsentEntity());
        // Then
        verify(consentJpaRepository).save(aisConsentCaptor.capture());

        ConsentEntity consent = aisConsentCaptor.getValue();
        assertEquals(ConsentStatus.EXPIRED, consent.getConsentStatus());
        assertEquals(TODAY, consent.getExpireDate());
        assertEquals(TODAY, consent.getLastActionDate());
    }

    @Test
    void updateConsentOnConfirmationExpiration() {
        // Given
        ArgumentCaptor<ConsentEntity> aisConsentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);

        // When
        expirationService.updateOnConfirmationExpiration(buildConsent());

        // Then
        verify(consentJpaRepository).save(aisConsentCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentCaptor.getValue().getConsentStatus());
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration_expired() {
        // Given
        ConsentEntity consent = buildConsent();
        consent.setCreationTimestamp(OffsetDateTime.now().minusHours(1));

        ArgumentCaptor<ConsentEntity> aisConsentCaptor = ArgumentCaptor.forClass(ConsentEntity.class);
        when(aspspProfileService.getAspspSettings(consent.getInstanceId())).thenReturn(buildAspspSettings(100L));

        // When
        expirationService.checkAndUpdateOnConfirmationExpiration(consent);

        // Then
        verify(consentJpaRepository).save(aisConsentCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentCaptor.getValue().getConsentStatus());
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration_nonExpired() {
        // Given
        ConsentEntity consent = buildConsent();

        when(aspspProfileService.getAspspSettings(consent.getInstanceId())).thenReturn(buildAspspSettings(86400L));

        // When
        ConsentEntity actual = expirationService.checkAndUpdateOnConfirmationExpiration(consent);

        // Then
        assertEquals(consent, actual);
    }

    @Test
    void updateConsentListOnConfirmationExpiration() {
        // Given
        ArgumentCaptor<List<ConsentEntity>> aisConsentListCaptor = ArgumentCaptor.forClass(List.class);

        // When
        expirationService.updateConsentListOnConfirmationExpiration(Collections.singletonList(buildConsent()));

        // Then
        verify(consentJpaRepository).saveAll(aisConsentListCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentListCaptor.getValue().get(0).getConsentStatus());
    }

    private ConsentEntity buildConsent() {
        ConsentEntity consent = new ConsentEntity();
        consent.setConsentStatus(ConsentStatus.RECEIVED);
        consent.setValidUntil(AisConsentConfirmationExpirationServiceTest.TOMORROW);
        return consent;
    }

    private AspspSettings buildAspspSettings(Long notConfirmedConsentExpirationTimeMs) {
        return new AspspSettings(new AisAspspProfileSetting(new ConsentTypeSetting(false, false, false, 0, notConfirmedConsentExpirationTimeMs, 0, false, false), null, null, null, null), null, null, null);
    }
}
