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

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentScheduleTaskTest {

    @InjectMocks
    private ConsentScheduleTask scheduleTask;

    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @Captor
    private ArgumentCaptor<ArrayList<ConsentEntity>> consentCaptor;

    @Test
    void checkConsentStatus_allConsentsExpired() {
        List<ConsentEntity> availableConsents = new ArrayList<>();
        availableConsents.add(createConsent(RECEIVED));
        availableConsents.add(createConsent(VALID));
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(availableConsents);

        when(consentJpaRepository.saveAll(consentCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        assertEquals(2, consentCaptor.getValue().size());
        consentCaptor.getValue().forEach(c -> assertEquals(EXPIRED, c.getConsentStatus()));

        verify(consentJpaRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(consentJpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void checkConsentStatus_notAllConsentsExpired() {
        List<ConsentEntity> availableConsents = new ArrayList<>();
        availableConsents.add(createConsent(RECEIVED));
        ConsentEntity consent = createConsent(VALID);
        consent.setValidUntil(LocalDate.now().plusDays(1));
        availableConsents.add(consent);
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(availableConsents);

        when(consentJpaRepository.saveAll(consentCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        assertEquals(1, consentCaptor.getValue().size());
        assertEquals("RECEIVED", consentCaptor.getValue().get(0).getExternalId());
        consentCaptor.getValue().forEach(c -> assertEquals(EXPIRED, c.getConsentStatus()));

        verify(consentJpaRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(consentJpaRepository, times(1)).saveAll(anyList());
    }

    @Test
    void checkConsentStatus_nullValue() {

        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(null);

        when(consentJpaRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        verify(consentJpaRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(consentJpaRepository, times(1)).saveAll(Collections.emptyList());
    }

    @NotNull
    private ConsentEntity createConsent(ConsentStatus consentStatus) {
        ConsentEntity aisConsent = new ConsentEntity();
        aisConsent.setConsentStatus(consentStatus);
        aisConsent.setExternalId(consentStatus.toString());
        aisConsent.setValidUntil(LocalDate.now().minusDays(1));
        return aisConsent;
    }
}
