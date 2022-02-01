/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.scheduler;

import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
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
    private ConsentJpaRepository consentJpaRepository;

    @Captor
    private ArgumentCaptor<ArrayList<String>> consentsCaptor;

    @Test
    void obsoleteNotConfirmedConsentIfExpired() {
        // Given
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED)))
            .thenReturn(getTestConsentEntityList());
        when(aisConsentConfirmationExpirationService.isConfirmationExpired(any(ConsentEntity.class)))
            .thenReturn(true, false, true);

        // When
        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        // Then
        verify(consentJpaRepository, times(1))
            .findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED));
        verify(aisConsentConfirmationExpirationService, times(2)).isConfirmationExpired(any(ConsentEntity.class));
        verify(aisConsentConfirmationExpirationService, times(1)).updateConsentListOnConfirmationExpirationByExternalIds(consentsCaptor.capture());

        assertEquals(1, consentsCaptor.getValue().size());
        assertEquals("first id", consentsCaptor.getValue().get(0));
    }

    @Test
    void obsoleteNotConfirmedConsentIfExpired_emptyList() {
        // Given
        when(consentJpaRepository.findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED)))
            .thenReturn(Collections.emptyList());

        // When
        scheduleTask.obsoleteNotConfirmedConsentIfExpired();

        // Then
        verify(consentJpaRepository, times(1))
            .findByConsentStatusIn(EnumSet.of(ConsentStatus.RECEIVED, ConsentStatus.PARTIALLY_AUTHORISED));
        verify(aisConsentConfirmationExpirationService, never()).isConfirmationExpired(any(ConsentEntity.class));
        verify(aisConsentConfirmationExpirationService, never()).updateConsentListOnConfirmationExpirationByExternalIds(anyList());
    }

    private List<ConsentEntity> getTestConsentEntityList() {
        ConsentEntity firstEntity = new ConsentEntity();
        firstEntity.setExternalId("first id");
        firstEntity.setSigningBasketBlocked(false);
        ConsentEntity secondEntity = new ConsentEntity();
        secondEntity.setExternalId("second id");
        secondEntity.setSigningBasketBlocked(false);
        ConsentEntity thirdEntity = new ConsentEntity();
        thirdEntity.setExternalId("third id");
        thirdEntity.setSigningBasketBlocked(true);
        return List.of(firstEntity, secondEntity, thirdEntity);
    }
}
