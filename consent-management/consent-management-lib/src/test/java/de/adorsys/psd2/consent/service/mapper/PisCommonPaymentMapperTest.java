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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentMapperTest {
    private final static String PSU_ID = "777";

    @InjectMocks
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private AccountReferenceMapper accountReferenceMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;

    @Test
    public void mapToGetPisAuthorizationResponse() {
        //Given
        when(accountReferenceMapper.mapToCmsAccountReference(any(AccountReferenceEntity.class))).thenReturn(null);
        when(psuDataMapper.mapToPsuIdDataList(any())).thenReturn(null);
        when(psuDataMapper.mapToPsuIdData(any())).thenReturn(new PsuIdData(PSU_ID, null, null, null));
        when(tppInfoMapper.mapToTppInfo(any(TppInfoEntity.class))).thenReturn(null);
        PisAuthorization pisAuthorization = buildPisAuthorization();
        PsuData psuData = new PsuData();
        psuData.setPsuId(PSU_ID);
        pisAuthorization.setPsuData(psuData);
        //When
        GetPisAuthorisationResponse getPisAuthorisationResponse = pisCommonPaymentMapper.mapToGetPisAuthorizationResponse(pisAuthorization);
        //Then
        assertEquals(getPisAuthorisationResponse.getPsuIdData().getPsuId(), psuData.getPsuId());
    }

    private PisAuthorization buildPisAuthorization() {
        PisAuthorization pisAuthorization = new PisAuthorization();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setDebtorAccount(null);
        pisCommonPaymentData.setPayments(Collections.singletonList(pisPaymentData));
        pisAuthorization.setPaymentData(pisCommonPaymentData);
        return pisAuthorization;
    }
}
