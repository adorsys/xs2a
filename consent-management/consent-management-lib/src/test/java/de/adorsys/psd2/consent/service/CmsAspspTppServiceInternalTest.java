/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import de.adorsys.psd2.consent.service.mapper.TppStopListMapper;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsAspspTppServiceInternalTest {
    private final String AUTHORISATION_NUMBER = "Authorisation number";
    private final String AUTHORITY_ID = "Authority id";
    private final Duration BLOCKING_DURATION = Duration.ofMillis(15000);
    private final String AUTHORISATION_NUMBER_NOT_EXISTING = "Not existing Authorisation number";
    private final String AUTHORITY_ID_NOT_EXISTING = "Not existing Authority id";
    private final String INSTANCE_ID = "Instance id";

    @InjectMocks
    private CmsAspspTppServiceInternal cmsAspspTppService;

    @Mock
    private TppStopListRepository stopListRepository;
    @Mock
    private TppStopListMapper tppStopListMapper;

    @Mock
    private TppStopListEntity tppStopListEntity;
    @Mock
    private TppStopListRecord tppStopListRecord;

    @Test
    public void getTppStopListRecord_Fail_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        Optional<TppStopListRecord> result = cmsAspspTppService.getTppStopListRecord(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID);

        assertFalse(result.isPresent());
    }

    @Test
    public void getTppStopListRecord_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        when(tppStopListMapper.mapToTppStopListRecord(tppStopListEntity))
            .thenReturn(tppStopListRecord);

        Optional<TppStopListRecord> result = cmsAspspTppService.getTppStopListRecord(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID);

        assertTrue(result.isPresent());
        assertEquals(tppStopListRecord, result.get());
    }

    @Test
    public void blockTpp_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        doNothing()
            .when(tppStopListEntity).block(BLOCKING_DURATION);

        when(stopListRepository.save(tppStopListEntity))
            .thenReturn(tppStopListEntity);

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID, BLOCKING_DURATION);

        assertTrue(isBlocked);
        verify(stopListRepository).save(tppStopListEntity);
    }

    @Test
    public void blockTpp_Success_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        TppStopListEntity entityToBeBlocked = buildBlockedTppStopListEntity(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID, null);

        when(stopListRepository.save(entityToBeBlocked))
            .thenReturn(entityToBeBlocked);

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID, null);

        assertTrue(isBlocked);
        verify(stopListRepository).save(entityToBeBlocked);
    }

    @Test
    public void unblockTpp_Success_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        boolean isUnblocked = cmsAspspTppService.unblockTpp(AUTHORISATION_NUMBER_NOT_EXISTING, AUTHORITY_ID_NOT_EXISTING, INSTANCE_ID);

        assertTrue(isUnblocked);
        verify(stopListRepository, never()).save(any(TppStopListEntity.class));
    }

    @Test
    public void unblockTpp_Success_TppEntityIsExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndNationalAuthorityIdAndInstanceId(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID))
            .thenReturn(Optional.of(tppStopListEntity));

        doNothing()
            .when(tppStopListEntity).unblock();

        when(stopListRepository.save(tppStopListEntity))
            .thenReturn(tppStopListEntity);

        boolean isUnblocked = cmsAspspTppService.unblockTpp(AUTHORISATION_NUMBER, AUTHORITY_ID, INSTANCE_ID);

        assertTrue(isUnblocked);
        verify(stopListRepository).save(tppStopListEntity);
    }

    private TppStopListEntity buildBlockedTppStopListEntity(String authorisationNumber, String authorityId, String instanceId, Duration blockingDuration) {
        TppStopListEntity entity = new TppStopListEntity();
        entity.setTppAuthorisationNumber(authorisationNumber);
        entity.setNationalAuthorityId(authorityId);
        entity.block(blockingDuration);
        return entity;
    }
}
