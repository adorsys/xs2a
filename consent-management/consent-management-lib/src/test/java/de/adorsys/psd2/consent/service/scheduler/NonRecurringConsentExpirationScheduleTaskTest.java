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
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NonRecurringConsentExpirationScheduleTaskTest {
    private static final LocalDate LOCAL_DATE = LocalDate.now();
    private static final OffsetDateTime CREATION_TIMESTAMP =
        OffsetDateTime.of(2019, 6, 4, 12, 20, 0, 0, ZoneOffset.UTC);
    private static final String CONSENT_ID_1 = "consent id 1";
    private static final String CONSENT_ID_2 = "consent id 2";

    @Mock
    private AisConsentRepository aisConsentRepository;
    @Captor
    private ArgumentCaptor<List<AisConsent>> aisConsentsCaptor;

    @InjectMocks
    private NonRecurringConsentExpirationScheduleTask nonRecurringConsentExpirationScheduleTask;

    @Test
    public void expireUsedNonRecurringConsent_expiresReceivedAndValidConsents() {
        // Given
        List<AisConsent> aisConsentList = Arrays.asList(buildAisConsent(CONSENT_ID_1, RECEIVED),
                                                        buildAisConsent(CONSENT_ID_2, VALID));
        doReturn(aisConsentList).when(aisConsentRepository)
            .findUsedNonRecurringConsents(EnumSet.of(RECEIVED, VALID), LOCAL_DATE);
        List<AisConsent> expiredConsentList = Arrays.asList(buildAisConsent(CONSENT_ID_1, EXPIRED),
                                                            buildAisConsent(CONSENT_ID_2, EXPIRED));

        // When
        nonRecurringConsentExpirationScheduleTask.expireUsedNonRecurringConsent();

        // Then
        verify(aisConsentRepository).save(aisConsentsCaptor.capture());
        assertEquals(expiredConsentList, aisConsentsCaptor.getValue());
    }

    private AisConsent buildAisConsent(String externalId, ConsentStatus status) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setExternalId(externalId);
        aisConsent.setConsentStatus(status);
        aisConsent.setCreationTimestamp(CREATION_TIMESTAMP);
        return aisConsent;
    }
}
