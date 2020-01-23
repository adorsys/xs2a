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

package de.adorsys.psd2.consent.service.aspsp;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.consent.service.mapper.TppInfoMapper;
import de.adorsys.psd2.consent.service.mapper.TppStopListMapper;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAspspTppServiceInternalTest {
    private final String AUTHORISATION_NUMBER = "Authorisation number";
    private final Duration BLOCKING_DURATION = Duration.ofMillis(15000);
    private final String AUTHORISATION_NUMBER_NOT_EXISTING = "Not existing Authorisation number";
    private final String INSTANCE_ID = "Instance id";

    @InjectMocks
    private CmsAspspTppServiceInternal cmsAspspTppService;

    @Mock
    private TppStopListRepository stopListRepository;
    @Mock
    private TppStopListMapper tppStopListMapper;
    @Mock
    private TppInfoRepository tppInfoRepository;
    @Mock
    private TppInfoMapper tppInfoMapper;

    @Mock
    private TppStopListEntity tppStopListEntity;
    @Mock
    private TppStopListRecord tppStopListRecord;
    @Mock
    private TppInfoEntity tppInfoEntity;
    @Mock
    private TppInfo tppInfo;

    @Test
    void getTppStopListRecord_Fail_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        Optional<TppStopListRecord> result = cmsAspspTppService.getTppStopListRecord(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void getTppStopListRecord_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListMapper.mapToTppStopListRecord(tppStopListEntity))
            .thenReturn(tppStopListRecord);

        Optional<TppStopListRecord> result = cmsAspspTppService.getTppStopListRecord(AUTHORISATION_NUMBER, INSTANCE_ID);

        assertTrue(result.isPresent());
        assertEquals(tppStopListRecord, result.get());
    }

    @Test
    void blockTpp_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        doNothing()
            .when(tppStopListEntity).block(BLOCKING_DURATION);

        when(stopListRepository.save(tppStopListEntity))
            .thenReturn(tppStopListEntity);

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER, INSTANCE_ID, BLOCKING_DURATION);

        assertTrue(isBlocked);
        verify(stopListRepository).save(tppStopListEntity);
    }

    @Test
    void blockTpp_Success_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        TppStopListEntity entityToBeBlocked = buildBlockedTppStopListEntity(AUTHORISATION_NUMBER_NOT_EXISTING, null);

        when(stopListRepository.save(entityToBeBlocked))
            .thenReturn(entityToBeBlocked);

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID, null);

        assertTrue(isBlocked);
        verify(stopListRepository).save(entityToBeBlocked);
    }

    @Test
    void unblockTpp_Success_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        boolean isUnblocked = cmsAspspTppService.unblockTpp(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID);

        assertTrue(isUnblocked);
        verify(stopListRepository, never()).save(any(TppStopListEntity.class));
    }

    @Test
    void unblockTpp_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        doNothing()
            .when(tppStopListEntity).unblock();

        when(stopListRepository.save(tppStopListEntity))
            .thenReturn(tppStopListEntity);

        boolean isUnblocked = cmsAspspTppService.unblockTpp(AUTHORISATION_NUMBER, INSTANCE_ID);

        assertTrue(isUnblocked);
        verify(stopListRepository).save(tppStopListEntity);
    }

    @Test
    void getTppInfoRecord_Fail_TppEntityIsNotExistInDB() {
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        Optional<TppInfo> result = cmsAspspTppService.getTppInfo(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void getTppInfoRecord_Success_TppEntityIsExistInDB() {
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER, INSTANCE_ID))
            .thenReturn(Optional.of(tppInfoEntity));

        when(tppInfoMapper.mapToTppInfo(tppInfoEntity))
            .thenReturn(tppInfo);

        Optional<TppInfo> result = cmsAspspTppService.getTppInfo(AUTHORISATION_NUMBER, INSTANCE_ID);

        assertTrue(result.isPresent());
        assertEquals(tppInfo, result.get());
    }

    private TppStopListEntity buildBlockedTppStopListEntity(String authorisationNumber, Duration blockingDuration) {
        TppStopListEntity entity = new TppStopListEntity();
        entity.setTppAuthorisationNumber(authorisationNumber);
        entity.block(blockingDuration);
        return entity;
    }
}
