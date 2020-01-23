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

import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppStopListScheduleTaskTest {

    @InjectMocks
    private TppStopListScheduleTask scheduleTask;

    @Mock
    private TppStopListRepository tppStopListRepository;

    @Captor
    private ArgumentCaptor<ArrayList<TppStopListEntity>> unblockedTppsCaptor;

    @Test
    void unblockTppIfBlockingExpired() {
        List<TppStopListEntity> tppStopList = new ArrayList<>();
        tppStopList.add(createTppStopEntity());
        tppStopList.add(createTppStopEntity());

        when(tppStopListRepository.findAllByStatusAndBlockingExpirationTimestampLessThanEqual(eq(TppStatus.BLOCKED), any(OffsetDateTime.class)))
            .thenReturn(tppStopList);
        when(tppStopListRepository.saveAll(unblockedTppsCaptor.capture())).thenReturn(Collections.emptyList());

        scheduleTask.unblockTppIfBlockingExpired();

        verify(tppStopListRepository, times(1)).findAllByStatusAndBlockingExpirationTimestampLessThanEqual(eq(TppStatus.BLOCKED), any(OffsetDateTime.class));
        verify(tppStopListRepository, times(1)).saveAll(anyList());

        assertEquals(2, unblockedTppsCaptor.getValue().size());
        unblockedTppsCaptor.getValue().forEach(tpp -> {
            assertEquals(TppStatus.ENABLED, tpp.getStatus());
            assertNull(tpp.getBlockingExpirationTimestamp());
        });
    }

    @Test
    void unblockTppIfBlockingExpired_emptyList() {
        List<TppStopListEntity> tppStopList = new ArrayList<>();
        tppStopList.add(createTppStopEntity());
        tppStopList.add(createTppStopEntity());

        when(tppStopListRepository.findAllByStatusAndBlockingExpirationTimestampLessThanEqual(eq(TppStatus.BLOCKED), any(OffsetDateTime.class)))
            .thenReturn(Collections.emptyList());

        scheduleTask.unblockTppIfBlockingExpired();

        verify(tppStopListRepository, times(1)).findAllByStatusAndBlockingExpirationTimestampLessThanEqual(eq(TppStatus.BLOCKED), any(OffsetDateTime.class));
        verify(tppStopListRepository, never()).saveAll(anyList());
    }

    @NotNull
    private TppStopListEntity createTppStopEntity() {
        TppStopListEntity tppStopListEntity = new TppStopListEntity();
        tppStopListEntity.block(Duration.ofDays(1));
        return tppStopListEntity;
    }
}
