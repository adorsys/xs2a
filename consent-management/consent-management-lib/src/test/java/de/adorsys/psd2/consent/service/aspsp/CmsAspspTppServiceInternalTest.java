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

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER, INSTANCE_ID, BLOCKING_DURATION);

        assertTrue(isBlocked);
    }

    @Test
    void blockTpp_Success_TppEntityIsNotExistInDB() {
        when(stopListRepository.findByTppAuthorisationNumberAndInstanceId(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID))
            .thenReturn(Optional.empty());

        boolean isBlocked = cmsAspspTppService.blockTpp(AUTHORISATION_NUMBER_NOT_EXISTING, INSTANCE_ID, null);

        assertTrue(isBlocked);
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

        boolean isUnblocked = cmsAspspTppService.unblockTpp(AUTHORISATION_NUMBER, INSTANCE_ID);

        assertTrue(isUnblocked);
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
}
