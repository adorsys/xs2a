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

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentConfirmationExpirationServiceTest {
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    @InjectMocks
    private AisConsentConfirmationExpirationService expirationService;
    @Mock
    private AisConsentRepository aisConsentRepository;

    @Test
    public void expireConsent() {
        //Given
        ArgumentCaptor<AisConsent> aisConsentCaptor = ArgumentCaptor.forClass(AisConsent.class);
        //When
        expirationService.expireConsent(new AisConsent());
        //Then
        verify(aisConsentRepository).save(aisConsentCaptor.capture());

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
}
