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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonRecurringConsentExpirationScheduleTaskTest {

    @InjectMocks
    private NonRecurringConsentExpirationScheduleTask scheduleTask;

    @Mock
    private AisConsentJpaRepository aisConsentJpaRepository;

    @Captor
    private ArgumentCaptor<ArrayList<AisConsent>> consentsCaptor;

    @Test
    void expireUsedNonRecurringConsent() {
        List<AisConsent> aisConsents = new ArrayList<>();
        aisConsents.add(createConsent(RECEIVED));
        aisConsents.add(createConsent(VALID));

        when(aisConsentJpaRepository.findUsedNonRecurringConsents(eq(EnumSet.of(RECEIVED, VALID)), any(LocalDate.class)))
            .thenReturn(aisConsents);
        when(aisConsentJpaRepository.saveAll(consentsCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.expireUsedNonRecurringConsent();

        verify(aisConsentJpaRepository, times(1)).findUsedNonRecurringConsents(eq(EnumSet.of(RECEIVED, VALID)), any(LocalDate.class));
        verify(aisConsentJpaRepository, times(1)).saveAll(anyList());

        assertEquals(2, consentsCaptor.getValue().size());
        consentsCaptor.getValue().forEach(c -> assertEquals(EXPIRED, c.getConsentStatus()));
    }

    @NotNull
    private AisConsent createConsent(ConsentStatus consentStatus) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentStatus(consentStatus);
        return aisConsent;
    }
}
