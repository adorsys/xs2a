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

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppServiceInternalTest {
    private final static String INSTANCE_ID = "UNDEFINED";
    private final static String AUTHORISATION_NUMBER = "HDIDF-SDKJHUD-767DHB";

    @InjectMocks
    private TppServiceInternal tppServiceInternal;
    @Mock
    private TppInfoRepository tppInfoRepository;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(tppServiceInternal, "serviceInstanceId", INSTANCE_ID);
    }

    @Test
    public void updateTppInfo_tppNotFound() {
        //Given
        TppInfo tppInfo = buildTppInfo(null);
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), INSTANCE_ID)).thenReturn(Optional.empty());
        //When
        boolean updateTppInfo = tppServiceInternal.updateTppInfo(tppInfo);
        //Then
        assertFalse(updateTppInfo);
    }

    @Test
    public void updateTppInfo_tppFoundAndUpdated() {
        //Given
        TppInfo tppInfo = buildTppInfo(buildTppRoles());
        TppInfoEntity tppInfoEntity = buildTppInfoEntity(null);
        ArgumentCaptor<TppInfoEntity> argument = ArgumentCaptor.forClass(TppInfoEntity.class);
        when(tppInfoRepository.findFirstByAuthorisationNumberAndInstanceId(tppInfo.getAuthorisationNumber(), INSTANCE_ID)).thenReturn(Optional.of(tppInfoEntity));
        //When
        boolean updateTppInfo = tppServiceInternal.updateTppInfo(tppInfo);
        //Then
        assertTrue(updateTppInfo);
        verify(tppInfoRepository).save(argument.capture());
        TppInfoEntity saved = argument.getValue();
        assertTrue(CollectionUtils.isEqualCollection(tppInfo.getTppRoles(), saved.getTppRoles()));
    }

    private TppInfo buildTppInfo(List<TppRole> roles) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        tppInfo.setTppRoles(roles);
        return tppInfo;
    }

    private TppInfoEntity buildTppInfoEntity(List<TppRole> roles) {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(AUTHORISATION_NUMBER);
        tppInfoEntity.setTppRoles(roles);
        return tppInfoEntity;
    }

    private List<TppRole> buildTppRoles() {
        return Arrays.asList(TppRole.AISP, TppRole.PISP, TppRole.PIISP);
    }
}
