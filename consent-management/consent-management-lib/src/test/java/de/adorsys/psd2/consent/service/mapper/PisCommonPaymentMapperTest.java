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

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.reader.JsonReader;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentMapperTest {
    private final static String PSU_ID = "777";
    private final static JsonReader jsonReader = new JsonReader();

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
        when(psuDataMapper.mapToPsuIdDataList(any())).thenReturn(null);
        when(psuDataMapper.mapToPsuIdData(any())).thenReturn(new PsuIdData(PSU_ID, null, null, null));
        PisAuthorization pisAuthorization = buildPisAuthorization();
        PsuData psuData = new PsuData();
        psuData.setPsuId(PSU_ID);
        pisAuthorization.setPsuData(psuData);
        //When
        GetPisAuthorisationResponse getPisAuthorisationResponse = pisCommonPaymentMapper.mapToGetPisAuthorizationResponse(pisAuthorization);
        //Then
        assertEquals(getPisAuthorisationResponse.getPsuIdData().getPsuId(), psuData.getPsuId());
    }

    @Test
    public void mapToPisPaymentDataList() {
        //Given
        PisPayment pisPayment = buildPisPayment(null);
        PisPaymentData pisPaymentDataExpected = jsonReader.getObjectFromFile("json/service/mapper/pis-payment-data.json", PisPaymentData.class);
        //When
        List<PisPaymentData> pisPaymentDataList = pisCommonPaymentMapper.mapToPisPaymentDataList(Collections.singletonList(pisPayment), null);
        //Then
        PisPaymentData pisPaymentDataActual = pisPaymentDataList.get(0);
        assertEquals(pisPaymentDataExpected, pisPaymentDataActual);
    }

    @Test
    public void mapToPisPaymentDataList_BatchBookingPreferred_True() {
        //Given
        PisPayment pisPayment = buildPisPayment(Boolean.TRUE);
        //When
        List<PisPaymentData> pisPaymentDataList = pisCommonPaymentMapper.mapToPisPaymentDataList(Collections.singletonList(pisPayment), null);
        //Then
        PisPaymentData pisPaymentData = pisPaymentDataList.get(0);
        assertEquals(pisPayment.getBatchBookingPreferred(), pisPaymentData.getBatchBookingPreferred());
    }

    @Test
    public void mapToPisPaymentDataList_BatchBookingPreferred_False() {
        //Given
        PisPayment pisPayment = buildPisPayment(Boolean.FALSE);
        //When
        List<PisPaymentData> pisPaymentDataList = pisCommonPaymentMapper.mapToPisPaymentDataList(Collections.singletonList(pisPayment), null);
        //Then
        PisPaymentData pisPaymentData = pisPaymentDataList.get(0);
        assertEquals(pisPayment.getBatchBookingPreferred(), pisPaymentData.getBatchBookingPreferred());
    }

    private PisAuthorization buildPisAuthorization() {
        PisAuthorization pisAuthorization = new PisAuthorization();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        pisPaymentData.setDebtorAccount(null);
        pisCommonPaymentData.setPayments(Collections.singletonList(pisPaymentData));
        pisAuthorization.setPaymentData(pisCommonPaymentData);
        return pisAuthorization;
    }

    private PisPayment buildPisPayment(Boolean batchBookingPreferred) {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        pisPayment.setBatchBookingPreferred(batchBookingPreferred);
        return pisPayment;
    }
}
