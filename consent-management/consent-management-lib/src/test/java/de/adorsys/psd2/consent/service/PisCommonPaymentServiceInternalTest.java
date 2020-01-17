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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceInternalTest {

    @InjectMocks
    private PisCommonPaymentServiceInternal pisCommonPaymentService;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private TppInfoRepository tppInfoRepository;
    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private CorePaymentsConvertService corePaymentsConvertService;


    private PisCommonPaymentData pisCommonPaymentData;
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final JsonReader jsonReader = new JsonReader();

    @Before
    public void setUp() {
        pisCommonPaymentData = buildPisCommonPaymentData();
    }

    @Test
    public void createCommonPayment_checkUpdateTppRoles() {
        // Given
        List<TppRole> roles = Arrays.asList(TppRole.AISP, TppRole.PISP, TppRole.PIISP);
        ArgumentCaptor<PisCommonPaymentData> argument = ArgumentCaptor.forClass(PisCommonPaymentData.class);

        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("tpp-id-1");
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setTppInfo(tppInfo);

        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setTppRoles(roles);

        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTppInfo(tppInfoEntity);

        when(pisCommonPaymentMapper.mapToPisCommonPaymentData(pisPaymentInfo))
            .thenReturn(pisCommonPaymentData);
        when(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).thenReturn(pisCommonPaymentData);

        // When
        pisCommonPaymentService.createCommonPayment(pisPaymentInfo);

        // Then
        verify(pisCommonPaymentDataRepository).save(argument.capture());
        assertEquals(roles, argument.getValue().getTppInfo().getTppRoles());
    }

    @Test
    public void updateMultilevelSca_ShouldReturnTrue() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<Boolean> actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    public void updateMultilevelSca_ShouldReturnFalse() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    public void getPisCommonPaymentStatusById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, actual.getPayload());
    }

    @Test
    public void getPisCommonPaymentStatusById_logicalError() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(null);

        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    public void getCommonPaymentById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentData))
            .thenReturn(Optional.of(pisCommonPaymentResponse));

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(pisCommonPaymentResponse, actual.getPayload());
    }

    @Test
    public void getCommonPaymentById_logicalError() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);
        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentData))
            .thenReturn(Optional.empty());

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    public void updateCommonPaymentStatusById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<Boolean> actual = pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    public void updateCommonPaymentStatusById_transactionStatusIsFinalised() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCC);
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<Boolean> actual = pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD);

        // Then
        assertTrue(actual.isSuccessful());
        assertFalse(actual.getPayload());
        assertEquals(TransactionStatus.ACCC, pisCommonPaymentData.getTransactionStatus());

        verify(pisCommonPaymentDataRepository, never()).save(any());
    }

    @Test
    public void updateCommonPayment() {
        // Given
        PisCommonPaymentRequest request = new PisCommonPaymentRequest();
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        request.setPaymentInfo(pisPaymentInfo);
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));

        PisCommonPaymentData updatedPisCommonPaymentData = new PisCommonPaymentData();
        when(pisCommonPaymentMapper.mapToPisCommonPaymentData(pisPaymentInfo)).thenReturn(updatedPisCommonPaymentData);

        // When
        CmsResponse<CmsResponse.VoidResponse> actual = pisCommonPaymentService.updateCommonPayment(request, PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(CmsResponse.voidResponse(), actual.getPayload());

        verify(pisCommonPaymentDataRepository).save(updatedPisCommonPaymentData);
    }

    @Test
    public void getPsuDataListByPaymentId_success() {
        // Given
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(psuDataMapper.mapToPsuIdDataList(pisCommonPaymentData.getPsuDataList())).thenReturn(Collections.emptyList());

        // When
        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertTrue(actual.getPayload().isEmpty());
    }

    @Test
    public void getPsuDataListByPaymentId_logicalError() {
        // Given
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(psuDataMapper.mapToPsuIdDataList(pisCommonPaymentData.getPsuDataList())).thenReturn(null);

        // When
        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    public void transferCorePaymentToCommonPayment_success() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisCommonPaymentData.setPayments(Collections.singletonList(pisPaymentData));
        pisCommonPaymentData.setPayment(null);

        PisPayment pisPayment = new PisPayment();
        when(pisCommonPaymentMapper.mapToPisPayment(pisPaymentData)).thenReturn(pisPayment);
        byte[] bytes = "content".getBytes();
        when(corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), pisCommonPaymentData.getPaymentType()))
            .thenReturn(bytes);
        when(pisCommonPaymentDataRepository.save(pisCommonPaymentData)).thenReturn(pisCommonPaymentData);

        pisCommonPaymentService.transferCorePaymentToCommonPayment(pisCommonPaymentResponse, pisCommonPaymentData);

        assertEquals(bytes, pisCommonPaymentResponse.getPaymentData());

        verify(pisCommonPaymentMapper, times(1)).mapToPisPayment(pisPaymentData);
        verify(corePaymentsConvertService, times(1)).buildPaymentData(Collections.singletonList(pisPayment), pisCommonPaymentData.getPaymentType());
        verify(pisCommonPaymentDataRepository, times(1)).save(pisCommonPaymentData);
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        return pisCommonPaymentData;
    }
}

