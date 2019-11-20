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

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
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

    private PisCommonPaymentData pisCommonPaymentData;
    private List<PisAuthorization> pisAuthorizationList = new ArrayList<>();

    private static final String EXTERNAL_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private final static PsuData PSU_DATA = new PsuData("id", "type", "corporate ID", "corporate type");
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @Before
    public void setUp() {
        pisCommonPaymentData = buildPisCommonPaymentData();
        pisAuthorizationList.add(buildPisAuthorisation(EXTERNAL_ID, PaymentAuthorisationType.CANCELLED));
        pisAuthorizationList.add(buildPisAuthorisation(AUTHORISATION_ID, PaymentAuthorisationType.CREATED));
    }

    @Test
    public void createCommonPayment_checkUpdateTppRoles() {
        //Given
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

        //When
        pisCommonPaymentService.createCommonPayment(pisPaymentInfo);

        //Then
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
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, actual.getPayload());
    }

    @Test
    public void getPisCommonPaymentStatusById_logicalError() {
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(null);

        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    public void getCommonPaymentById_success() {
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentData))
            .thenReturn(Optional.of(pisCommonPaymentResponse));

        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        assertTrue(actual.isSuccessful());
        assertEquals(pisCommonPaymentResponse, actual.getPayload());
    }

    @Test
    public void getCommonPaymentById_logicalError() {
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);
        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentData))
            .thenReturn(Optional.empty());

        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    public void updateCommonPaymentStatusById_success() {
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        CmsResponse<Boolean> actual = pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD);

        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    public void updateCommonPaymentStatusById_transactionStatusIsFinalised() {
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCC);
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        CmsResponse<Boolean> actual = pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD);

        assertTrue(actual.isSuccessful());
        assertFalse(actual.getPayload());
        assertEquals(TransactionStatus.ACCC, pisCommonPaymentData.getTransactionStatus());

        verify(pisCommonPaymentDataRepository, never()).save(any());
    }

    @Test
    public void updateCommonPayment() {
        PisCommonPaymentRequest request = new PisCommonPaymentRequest();
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentMapper.mapToPisPaymentDataList(request.getPayments(), pisCommonPaymentData)).thenReturn(Collections.emptyList());

        CmsResponse<CmsResponse.VoidResponse> actual = pisCommonPaymentService.updateCommonPayment(request, PAYMENT_ID);

        assertTrue(actual.isSuccessful());
        assertEquals(CmsResponse.voidResponse(), actual.getPayload());

        verify(pisPaymentDataRepository).saveAll(Collections.emptyList());
    }

    @Test
    public void getPsuDataListByPaymentId_success() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(psuDataMapper.mapToPsuIdDataList(pisCommonPaymentData.getPsuDataList())).thenReturn(Collections.emptyList());


        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID);

        assertTrue(actual.isSuccessful());
        assertTrue(actual.getPayload().isEmpty());
    }

    @Test
    public void getPsuDataListByPaymentId_logicalError() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(psuDataMapper.mapToPsuIdDataList(pisCommonPaymentData.getPsuDataList())).thenReturn(null);


        CmsResponse<List<PsuIdData>> actual = pisCommonPaymentService.getPsuDataListByPaymentId(PAYMENT_ID);

        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        return buildPisCommonPaymentData(new AuthorisationTemplateEntity());
    }

    private PisCommonPaymentData buildPisCommonPaymentData(AuthorisationTemplateEntity authorisationTemplateEntity) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setId(1L);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setTransactionStatus(RCVD);
        pisCommonPaymentData.setAuthorizations(pisAuthorizationList);
        pisCommonPaymentData.setAuthorisationTemplate(authorisationTemplateEntity);
        pisCommonPaymentData.setInternalRequestId(INTERNAL_REQUEST_ID);
        return pisCommonPaymentData;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, PaymentAuthorisationType authorisationType) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(externalId);
        pisAuthorization.setAuthorizationType(authorisationType);
        pisAuthorization.setScaStatus(SCA_STATUS);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        pisAuthorization.setPsuData(PSU_DATA);
        return pisAuthorization;
    }
}

