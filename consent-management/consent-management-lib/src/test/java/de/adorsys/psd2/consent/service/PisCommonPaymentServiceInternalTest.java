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

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentServiceInternalTest {

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
    @Mock
    private AuthorisationRepository authorisationRepository;

    private PisCommonPaymentData pisCommonPaymentData;
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() {
        pisCommonPaymentData = buildPisCommonPaymentData();
    }

    @Test
    void createCommonPayment_checkUpdateTppRoles() {
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
    void updateMultilevelSca_ShouldReturnTrue() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));

        // When
        CmsResponse<Boolean> actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertTrue(actualResponse.getPayload());
    }

    @Test
    void updateMultilevelSca_ShouldReturnFalse() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.empty());

        // When
        CmsResponse<Boolean> actualResponse = pisCommonPaymentService.updateMultilevelSca(PAYMENT_ID, true);

        // Then
        assertTrue(actualResponse.isSuccessful());
        assertFalse(actualResponse.getPayload());
    }

    @Test
    void getPisCommonPaymentStatusById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, actual.getPayload());
    }

    @Test
    void getPisCommonPaymentStatusById_logicalError() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(null);

        // When
        CmsResponse<TransactionStatus> actual = pisCommonPaymentService.getPisCommonPaymentStatusById(PAYMENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    void getCommonPaymentById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();

        List<AuthorisationEntity> authorisations = buildAuthorisations();
        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(authorisations);

        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentData, authorisations))
            .thenReturn(Optional.of(pisCommonPaymentResponse));

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(pisCommonPaymentResponse, actual.getPayload());
    }

    @Test
    void getCommonPaymentById_logicalError() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<PisCommonPaymentResponse> actual = pisCommonPaymentService.getCommonPaymentById(PAYMENT_ID);

        // Then
        assertTrue(actual.hasError());
        assertEquals(CmsError.LOGICAL_ERROR, actual.getError());
    }

    @Test
    void updateCommonPaymentStatusById_success() {
        // Given
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
            .thenReturn(pisCommonPaymentData);

        // When
        CmsResponse<Boolean> actual = pisCommonPaymentService.updateCommonPaymentStatusById(PAYMENT_ID, TransactionStatus.RCVD);

        // Then
        assertTrue(actual.isSuccessful());
        assertEquals(TransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    void updateCommonPaymentStatusById_transactionStatusIsFinalised() {
        // Given
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.ACCC);
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(pisCommonPaymentData));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData))
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
    void getPsuDataListByPaymentId_success() {
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
    void getPsuDataListByPaymentId_logicalError() {
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
    void transferCorePaymentToCommonPayment_success() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisCommonPaymentData.setPayments(Collections.singletonList(pisPaymentData));
        pisCommonPaymentData.setPayment(null);

        PisPayment pisPayment = new PisPayment();
        when(pisCommonPaymentMapper.mapToPisPayment(pisPaymentData)).thenReturn(pisPayment);
        byte[] bytes = "content".getBytes();
        when(corePaymentsConvertService.buildPaymentData(Collections.singletonList(pisPayment), pisCommonPaymentData.getPaymentType()))
            .thenReturn(bytes);

        pisCommonPaymentService.transferCorePaymentToCommonPayment(pisCommonPaymentResponse, pisCommonPaymentData);

        assertEquals(bytes, pisCommonPaymentResponse.getPaymentData());

        verify(pisCommonPaymentMapper, times(1)).mapToPisPayment(pisPaymentData);
        verify(corePaymentsConvertService, times(1)).buildPaymentData(Collections.singletonList(pisPayment), pisCommonPaymentData.getPaymentType());
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.setAuthorisationTemplate(new AuthorisationTemplateEntity());
        return pisCommonPaymentData;
    }

    private List<AuthorisationEntity> buildAuthorisations() {
        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setType(AuthorisationType.PIS_CREATION);
        return Collections.singletonList(pisAuthorization);
    }
}

