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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TppServiceInternalTest {
    private final static String INSTANCE_ID = "UNDEFINED";

    @InjectMocks
    private TppServiceInternal tppServiceInternal;
    @Mock
    private TppInfoRepository tppInfoRepository;
    private final JsonReader jsonReader = new JsonReader();
    private TppInfo tppInfo;
    private TppInfoEntity tppInfoEntity;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tppServiceInternal, "serviceInstanceId", INSTANCE_ID);
        tppInfo = jsonReader.getObjectFromFile("json/service/tpp-info.json", TppInfo.class);
        tppInfoEntity = jsonReader.getObjectFromFile("json/service/tpp-info-entity.json", TppInfoEntity.class);
    }

    @Test
    void updateTppInfo_tppNotFound() {
        //Given
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), INSTANCE_ID)).thenReturn(Optional.empty());
        //When
        CmsResponse<Boolean> updateTppInfo = tppServiceInternal.updateTppInfo(tppInfo);
        //Then
        assertTrue(updateTppInfo.isSuccessful());

        assertFalse(updateTppInfo.getPayload());
        verify(tppInfoRepository, never()).save(any(TppInfoEntity.class));
    }

    @Test
    void updateTppInfo_tppFoundAndUpdated() {
        //Given
        tppInfoEntity.setTppRoles(null);
        ArgumentCaptor<TppInfoEntity> argument = ArgumentCaptor.forClass(TppInfoEntity.class);
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), INSTANCE_ID)).thenReturn(Optional.of(tppInfoEntity));
        //When
        CmsResponse<Boolean> updateTppInfo = tppServiceInternal.updateTppInfo(tppInfo);
        //Then
        assertTrue(updateTppInfo.isSuccessful());

        assertTrue(updateTppInfo.getPayload());

        verify(tppInfoRepository).save(argument.capture());
        TppInfoEntity saved = argument.getValue();
        assertTrue(CollectionUtils.isEqualCollection(tppInfo.getTppRoles(), saved.getTppRoles()));
    }

    @Test
    void updateTppInfo_tppFoundAndRolesNotChangedAndUpdated() {
        //Given
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), INSTANCE_ID)).thenReturn(Optional.of(tppInfoEntity));
        //When
        CmsResponse<Boolean> updateTppInfo = tppServiceInternal.updateTppInfo(tppInfo);
        //Then
        assertTrue(updateTppInfo.isSuccessful());

        assertTrue(updateTppInfo.getPayload());

        verify(tppInfoRepository, never()).save(any(TppInfoEntity.class));
    }
}
