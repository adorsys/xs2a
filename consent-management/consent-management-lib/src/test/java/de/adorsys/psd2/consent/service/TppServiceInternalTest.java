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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.xs2a.reader.JsonReader;
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
