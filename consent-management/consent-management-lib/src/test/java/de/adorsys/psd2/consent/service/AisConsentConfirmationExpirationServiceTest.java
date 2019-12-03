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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.ais.AisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.domain.ais.ConsentTypeSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentConfirmationExpirationServiceTest {
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    @InjectMocks
    private AisConsentConfirmationExpirationService expirationService;

    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;
    @Mock
    private AspspProfileService aspspProfileService;

    @Test
    public void expireConsent() {
        // Given
        ArgumentCaptor<AisConsent> aisConsentCaptor = ArgumentCaptor.forClass(AisConsent.class);
        // When
        expirationService.expireConsent(new AisConsent());
        // Then
        verify(aisConsentJpaRepository).save(aisConsentCaptor.capture());

        AisConsent aisConsent = aisConsentCaptor.getValue();
        assertEquals(ConsentStatus.EXPIRED, aisConsent.getConsentStatus());
        assertEquals(TODAY, aisConsent.getExpireDate());
        assertEquals(TODAY, aisConsent.getLastActionDate());
    }

    @Test
    public void isConsentExpiredOrFinalised_False() {
        Stream.of(null,
                  buildAisConsent(ConsentStatus.REJECTED, TOMORROW),
                  buildAisConsent(ConsentStatus.RECEIVED, TOMORROW)) //Given
            .map(expirationService::isConsentExpiredOrFinalised) //When
            .forEach(Assert::assertFalse); //Then
    }

    @Test
    public void isConsentExpiredOrFinalised_True() {
        Stream.of(buildAisConsent(ConsentStatus.RECEIVED, YESTERDAY),
                  buildNonReccuringAlreadyUsedAisConsent(ConsentStatus.RECEIVED, TOMORROW)) //Given
            .map(expirationService::isConsentExpiredOrFinalised) //When
            .forEach(Assert::assertTrue); //Then
    }

    @Test
    public void updateConsentOnConfirmationExpiration() {
        // Given
        ArgumentCaptor<AisConsent> aisConsentCaptor = ArgumentCaptor.forClass(AisConsent.class);

        // When
        expirationService.updateConsentOnConfirmationExpiration(buildAisConsent(ConsentStatus.RECEIVED, TOMORROW));

        // Then
        verify(aisConsentJpaRepository).save(aisConsentCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentCaptor.getValue().getConsentStatus());
    }

    @Test
    public void checkAndUpdateOnConfirmationExpiration_expired() {
        // Given
        ArgumentCaptor<AisConsent> aisConsentCaptor = ArgumentCaptor.forClass(AisConsent.class);
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(100L));

        AisConsent consent = buildAisConsent(ConsentStatus.RECEIVED, TOMORROW);
        consent.setCreationTimestamp(OffsetDateTime.now().minusHours(1));

        // When
        expirationService.checkAndUpdateOnConfirmationExpiration(consent);

        // Then
        verify(aisConsentJpaRepository).save(aisConsentCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentCaptor.getValue().getConsentStatus());
    }

    @Test
    public void checkAndUpdateOnConfirmationExpiration_nonExpired() {
        // Given
        when(aspspProfileService.getAspspSettings()).thenReturn(buildAspspSettings(86400L));

        AisConsent consent = buildAisConsent(ConsentStatus.RECEIVED, TOMORROW);
        // When
        AisConsent actual = expirationService.checkAndUpdateOnConfirmationExpiration(consent);

        // Then
        assertEquals(consent, actual);
    }

    @Test
    public void updateConsentListOnConfirmationExpiration() {
        // Given
        ArgumentCaptor<List<AisConsent>> aisConsentListCaptor = ArgumentCaptor.forClass(List.class);

        // When
        expirationService.updateConsentListOnConfirmationExpiration(Collections.singletonList(buildAisConsent(ConsentStatus.RECEIVED, TOMORROW)));

        // Then
        verify(aisConsentJpaRepository).saveAll(aisConsentListCaptor.capture());
        assertEquals(ConsentStatus.REJECTED, aisConsentListCaptor.getValue().get(0).getConsentStatus());
    }

    private AisConsent buildAisConsent(ConsentStatus consentStatus, LocalDate expireDate) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentStatus(consentStatus);
        aisConsent.setExpireDate(expireDate);
        return aisConsent;
    }

    private AisConsent buildNonReccuringAlreadyUsedAisConsent(ConsentStatus consentStatus, LocalDate expireDate) {
        AisConsent aisConsent = buildAisConsent(consentStatus, expireDate);
        aisConsent.setRecurringIndicator(false);
        AisConsentUsage usage = new AisConsentUsage();
        usage.setUsageDate(YESTERDAY);
        aisConsent.setUsages(Collections.singletonList(usage));
        return aisConsent;
    }

    private AspspSettings buildAspspSettings(Long notConfirmedConsentExpirationTimeMs) {
        return new AspspSettings(new AisAspspProfileSetting(new ConsentTypeSetting(false, false, false, 0, notConfirmedConsentExpirationTimeMs, 0, false), null, null, null, null), null, null, null);
    }
}
