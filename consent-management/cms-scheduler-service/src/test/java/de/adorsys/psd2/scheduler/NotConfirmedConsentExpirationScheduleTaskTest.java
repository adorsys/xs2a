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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentJpaRepository;
import de.adorsys.psd2.consent.service.AisConsentConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotConfirmedConsentExpirationScheduleTaskTest {

    @InjectMocks
    private NotConfirmedConsentExpirationScheduleTask scheduleTask;

    @Mock
    private AisConsentConfirmationExpirationService aisConsentConfirmationExpirationService;
    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;

    @Captor
    private ArgumentCaptor<ArrayList<AisConsent>> consentsCaptor;

    @Test
    void obsoleteNotConfirmedConsentIfExpired() {
        List<AisConsent> aisConsents = new ArrayList<>();
        aisConsents.add(new AisConsent());
        aisConsents.add(new AisConsent());

        when(aisConsentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED)))
            .thenReturn(aisConsents);
        when(aisConsentConfirmationExpirationService.isConfirmationExpired(any(AisConsent.class))).thenReturn(true, false);
        when(aisConsentConfirmationExpirationService.updateConsentListOnConfirmationExpiration(consentsCaptor.capture()))
            .thenReturn(Collections.emptyList());

        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        verify(aisConsentJpaRepository, times(1)).findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED));
        verify(aisConsentConfirmationExpirationService, times(2)).isConfirmationExpired(any(AisConsent.class));
        verify(aisConsentConfirmationExpirationService, times(1)).updateConsentListOnConfirmationExpiration(anyList());

        assertEquals(1, consentsCaptor.getValue().size());
    }

    @Test
    void obsoleteNotConfirmedConsentIfExpired_emptyList() {
        when(aisConsentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED)))
            .thenReturn(Collections.emptyList());

        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        verify(aisConsentJpaRepository, times(1)).findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED));
        verify(aisConsentConfirmationExpirationService, never()).isConfirmationExpired(any(AisConsent.class));
        verify(aisConsentConfirmationExpirationService, never()).updateConsentListOnConfirmationExpiration(anyList());
    }
}
