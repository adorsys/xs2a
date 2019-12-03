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

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentScheduleTaskTest {

    @InjectMocks
    private ConsentScheduleTask scheduleTask;

    @Mock
    private AisConsentRepository aisConsentRepository;

    @Captor
    private ArgumentCaptor<ArrayList<AisConsent>> consentCaptor;

    @Test
    public void checkConsentStatus_allConsentsExpired() {
        List<AisConsent> availableConsents = new ArrayList<>();
        availableConsents.add(createConsent(RECEIVED));
        availableConsents.add(createConsent(VALID));
        when(aisConsentRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(availableConsents);

        when(aisConsentRepository.saveAll(consentCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        assertEquals(2, consentCaptor.getValue().size());
        consentCaptor.getValue().forEach(c -> assertEquals(EXPIRED, c.getConsentStatus()));

        verify(aisConsentRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(aisConsentRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void checkConsentStatus_notAllConsentsExpired() {
        List<AisConsent> availableConsents = new ArrayList<>();
        availableConsents.add(createConsent(RECEIVED));
        AisConsent consent = createConsent(VALID);
        consent.setExpireDate(LocalDate.now().plusDays(1));
        availableConsents.add(consent);
        when(aisConsentRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(availableConsents);

        when(aisConsentRepository.saveAll(consentCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        assertEquals(1, consentCaptor.getValue().size());
        assertEquals("RECEIVED", consentCaptor.getValue().get(0).getExternalId());
        consentCaptor.getValue().forEach(c -> assertEquals(EXPIRED, c.getConsentStatus()));

        verify(aisConsentRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(aisConsentRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void checkConsentStatus_nullValue() {

        when(aisConsentRepository.findByConsentStatusIn(EnumSet.of(RECEIVED, VALID))).thenReturn(null);

        when(aisConsentRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        scheduleTask.checkConsentStatus();

        verify(aisConsentRepository, times(1)).findByConsentStatusIn(EnumSet.of(RECEIVED, VALID));
        verify(aisConsentRepository, times(1)).saveAll(Collections.emptyList());
    }

    @NotNull
    private AisConsent createConsent(ConsentStatus consentStatus) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentStatus(consentStatus);
        aisConsent.setExternalId(consentStatus.toString());
        aisConsent.setExpireDate(LocalDate.now().minusDays(1));
        return aisConsent;
    }
}
