/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.VALID;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NonRecurringConsentExpirationScheduleTaskTest {

    @InjectMocks
    private NonRecurringConsentExpirationScheduleTask scheduleTask;

    @Mock
    private ConsentJpaRepository consentJpaRepository;

    @Test
    void expireUsedNonRecurringConsent() {
        scheduleTask.expireUsedNonRecurringConsent();
        verify(consentJpaRepository, times(1)).expireUsedNonRecurringConsents(EnumSet.of(RECEIVED, VALID));
    }
}
