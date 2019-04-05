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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisAuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.PATC;
import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RCVD;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisCommonPaymentServiceInternalTest {

    @InjectMocks
    private PisCommonPaymentServiceInternal pisCommonPaymentService;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisAuthorisationRepository pisAuthorisationRepository;
    @Mock
    SecurityDataService securityDataService;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Spy
    private PsuDataMapper psuDataMapper;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private PisCommonPaymentConfirmationExpirationService pisCommonPaymentConfirmationExpirationService;
    @Mock
    private CmsPsuService cmsPsuService;

    private PisCommonPaymentData pisCommonPaymentData;
    private List<PisAuthorization> pisAuthorizationList = new ArrayList<>();
    private PisAuthorization pisAuthorization;

    private PisPaymentData pisPaymentData;
    private final long PIS_PAYMENT_DATA_ID = 1;
    private static final String EXTERNAL_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String PAYMENT_ID_WRONG = "5bbdcb28ca10e8e14a41b12f";
    private static final String PAYMENT_ID_WRONG_TRANSACTION_STATUS = "6bbdcb28ca10e8e14a41b12f";
    private static final String FINALISED_AUTHORISATION_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_CANCELLATION_AUTHORISATION_ID = "2a112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.STARTED;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("id", "type", "corporate ID", "corporate type");
    private final static PsuData PSU_DATA = new PsuData("id", "type", "corporate ID", "corporate type");

    private static final CreatePisAuthorisationRequest CREATE_PIS_AUTHORISATION_REQUEST = new CreatePisAuthorisationRequest(CmsAuthorisationType.CREATED, PSU_ID_DATA, ScaApproach.REDIRECT);

    @Before
    public void setUp() {
        when(psuDataMapper.mapToPsuData(any(PsuIdData.class))).thenCallRealMethod();
        pisAuthorization = buildPisAuthorisation(EXTERNAL_ID, CmsAuthorisationType.CREATED);
        pisCommonPaymentData = buildPisCommonPaymentData();
        pisPaymentData = buildPaymentData(pisCommonPaymentData);
        pisAuthorizationList.add(buildPisAuthorisation(EXTERNAL_ID, CmsAuthorisationType.CANCELLED));
        pisAuthorizationList.add(buildPisAuthorisation(AUTHORISATION_ID, CmsAuthorisationType.CREATED));
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.of(pisAuthorization));

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongPaymentId() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID_WRONG, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID, WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationByPaymentIdSuccess() {
        //When
        when(securityDataService.decryptId(PAYMENT_ID)).thenReturn(Optional.of(PAYMENT_ID));
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID, Arrays.asList(RCVD, PATC))).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisPaymentData.getPaymentData())).thenReturn(pisPaymentData.getPaymentData());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisCommonPaymentService.getAuthorisationsByPaymentId(PAYMENT_ID, CmsAuthorisationType.CANCELLED);
        //Assert
        assertTrue(authorizationByPaymentId.isPresent());
        assertEquals(1, authorizationByPaymentId.get().size());
        assertEquals(pisAuthorizationList.get(0).getExternalId(), authorizationByPaymentId.get().get(0));
    }

    @Test
    public void getAuthorisationByPaymentIdWrongPaymentId() {
        //When
        when(securityDataService.decryptId(PAYMENT_ID_WRONG)).thenReturn(Optional.empty());
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID_WRONG, Arrays.asList(RCVD, PATC))).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentIdAndTransactionStatusIn(PAYMENT_ID_WRONG, Arrays.asList(RCVD, PATC))).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisCommonPaymentService.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG, CmsAuthorisationType.CANCELLED);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }

    @Test
    public void getAuthorisationByPaymentIdWrongTransactionStatus() {
        //When
        when(securityDataService.decryptId(PAYMENT_ID)).thenReturn(Optional.of(PAYMENT_ID_WRONG_TRANSACTION_STATUS));
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID_WRONG_TRANSACTION_STATUS, Arrays.asList(RCVD, PATC))).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentIdAndTransactionStatusIn(PAYMENT_ID_WRONG_TRANSACTION_STATUS, Arrays.asList(RCVD, PATC))).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisCommonPaymentService.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG_TRANSACTION_STATUS, CmsAuthorisationType.CREATED);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }

    @Test
    public void updateConsentAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.STARTED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(expectedScaStatus);
        PisAuthorization finalisedConsentAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(FINALISED_AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(finalisedConsentAuthorization));

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisCommonPaymentService.updatePisAuthorisation(FINALISED_AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertNotEquals(updatePisCommonPaymentPsuDataResponse.get().getScaStatus(), expectedScaStatus);
    }

    @Test
    public void updateConsentAuthorisation_Success() {
        //Given
        PsuIdData psuIdData = new PsuIdData("new id", "new type", "new corporate ID", "new corporate type");
        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus.STARTED);
        updatePisCommonPaymentPsuDataRequest.setPsuData(psuIdData);
        PsuData expectedPsu = psuDataMapper.mapToPsuData(psuIdData);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(PAYMENT_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(pisAuthorization));
        when(pisAuthorisationRepository.save(pisAuthorization)).thenReturn(pisAuthorization);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.ofNullable(expectedPsu));
        when(cmsPsuService.isPsuDataRequestCorrect(any(), any()))
            .thenReturn(true);

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisCommonPaymentService.updatePisAuthorisation(PAYMENT_ID, updatePisCommonPaymentPsuDataRequest);
        verify(pisAuthorisationRepository).save(argument.capture());
        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertTrue(argument.getValue().getPsuData().contentEquals(expectedPsu));
    }

    @Test
    public void updatePisAuthorisation_startedStatus_shouldUpdatePsuDataInPayment() {
        //Given
        ArgumentCaptor<PisAuthorization> savedAuthorisationCaptor = ArgumentCaptor.forClass(PisAuthorization.class);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest =
            buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus.STARTED, PSU_ID_DATA);
        List<PsuData> psuDataList = Collections.singletonList(PSU_DATA);

        when(cmsPsuService.enrichPsuData(PSU_DATA, Collections.emptyList()))
            .thenReturn(psuDataList);
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.isPsuDataRequestCorrect(any(), any()))
            .thenReturn(true);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(PAYMENT_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(pisAuthorization));
        when(pisAuthorisationRepository.save(pisAuthorization))
            .thenReturn(pisAuthorization);

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> response =
            pisCommonPaymentService.updatePisAuthorisation(PAYMENT_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(response.isPresent());

        verify(pisAuthorisationRepository).save(savedAuthorisationCaptor.capture());
        PisAuthorization savedAuthorisation = savedAuthorisationCaptor.getValue();

        assertEquals(PSU_DATA, savedAuthorisation.getPsuData());
        assertEquals(psuDataList, savedAuthorisation.getPaymentData().getPsuDataList());
    }

    @Test
    public void updateConsentCancellationAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.STARTED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        PisAuthorization finalisedCancellationAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(expectedScaStatus);

        when(pisAuthorisationRepository.findByExternalIdAndAuthorizationType(FINALISED_CANCELLATION_AUTHORISATION_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.of(finalisedCancellationAuthorization));

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisCommonPaymentService.updatePisCancellationAuthorisation(FINALISED_CANCELLATION_AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertNotEquals(updatePisCommonPaymentPsuDataResponse.get().getScaStatus(), expectedScaStatus);

    }

    @Test
    public void createAuthorizationWithClosingPreviousAuthorisations_success() {
        //Given
        ArgumentCaptor<PisAuthorization> argument = ArgumentCaptor.forClass(PisAuthorization.class);
        //noinspection unchecked
        ArgumentCaptor<List<PisAuthorization>> failedAuthorisationsArgument = ArgumentCaptor.forClass((Class) List.class);
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(pisAuthorisationRepository.save(any(PisAuthorization.class))).thenReturn(pisAuthorization);
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatusIn(PAYMENT_ID, Arrays.asList(RCVD, PATC))).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisPaymentData.getPaymentData())).thenReturn(pisPaymentData.getPaymentData());
        when(cmsPsuService.definePsuDataForAuthorisation(any(), any())).thenReturn(Optional.of(PSU_DATA));
        when(cmsPsuService.enrichPsuData(any(), any())).thenReturn(Collections.singletonList(PSU_DATA));

        // When
        Optional<CreatePisAuthorisationResponse> actual = pisCommonPaymentService.createAuthorization(PAYMENT_ID, CREATE_PIS_AUTHORISATION_REQUEST);

        // Then
        assertTrue(actual.isPresent());
        verify(pisAuthorisationRepository).save(argument.capture());
        assertSame(argument.getValue().getScaStatus(), ScaStatus.STARTED);
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return new AspspSettings(1, false, false, null, null,
                                 null, false, null, null, 1, 1, false,
                                 false, false, false, false, false, 1, null,
                                 1, 1, null, 1, false, false, false, false, null);
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus status) {
        return buildUpdatePisCommonPaymentPsuDataRequest(status, null);
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus status, PsuIdData psuIdData) {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorizationId(FINALISED_AUTHORISATION_ID);
        request.setScaStatus(status);
        request.setPsuData(psuIdData);
        return request;
    }

    private PisAuthorization buildFinalisedConsentAuthorisation(ScaStatus status) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        pisAuthorization.setScaStatus(status);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        return pisAuthorization;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setId(PIS_PAYMENT_DATA_ID);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setTransactionStatus(RCVD);
        pisCommonPaymentData.setAuthorizations(pisAuthorizationList);
        return pisCommonPaymentData;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, CmsAuthorisationType authorisationType) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(externalId);
        pisAuthorization.setAuthorizationType(authorisationType);
        pisAuthorization.setScaStatus(SCA_STATUS);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        pisAuthorization.setPsuData(PSU_DATA);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        return pisAuthorization;
    }

    private PisPaymentData buildPaymentData(PisCommonPaymentData pisCommonPaymentData) {
        PisPaymentData paymentData = new PisPaymentData();
        paymentData.setPaymentId(PAYMENT_ID);
        paymentData.setPaymentData(pisCommonPaymentData);
        return paymentData;
    }
}
