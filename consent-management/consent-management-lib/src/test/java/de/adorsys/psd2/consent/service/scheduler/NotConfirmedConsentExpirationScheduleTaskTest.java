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

package de.adorsys.psd2.consent.service.scheduler;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotConfirmedConsentExpirationScheduleTaskTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @InjectMocks
    private NotConfirmedConsentExpirationScheduleTask notConfirmedConsentExpirationScheduleTask;

    @Mock
    private AisConsentRepository aisConsentRepository;
    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;

    @Test
    void obsoleteNotConfirmedConsentIfExpired() {
        // Given
        ArgumentCaptor<List<AisConsent>> aisConsentListCaptor = ArgumentCaptor.forClass(List.class);
        when(aisConsentRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED))).thenReturn(Collections.singletonList(buildAisConsent()));
        when(aisConsentConfirmationExpirationService.isConsentConfirmationExpired(any())).thenReturn(true);

        // When
        notConfirmedConsentExpirationScheduleTask.obsoleteNotConfirmedConsentIfExpired();

        // Then
        verify(aisConsentConfirmationExpirationService).updateConsentListOnConfirmationExpiration(aisConsentListCaptor.capture());
    }

    private AisConsent buildAisConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        aisConsent.setExpireDate(NotConfirmedConsentExpirationScheduleTaskTest.TOMORROW);
        return aisConsent;
    }
}
