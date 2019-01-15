/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
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
import java.util.stream.Collectors;

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
    private PisAuthorizationRepository pisAuthorizationRepository;
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

    private PisCommonPaymentData pisCommonPaymentData;
    private List<PisAuthorization> pisAuthorizationList = new ArrayList<>();
    private PisAuthorization pisAuthorization;

    private PisPaymentData pisPaymentData;
    private final long PIS_PAYMENT_DATA_ID = 1;
    private static final String EXTERNAL_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String PAYMENT_ID_WRONG = "5bbdcb28ca10e8e14a41b12f";
    private static final String FINALISED_AUTHORISATION_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_CANCELLATION_AUTHORISATION_ID = "2a112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final CmsAuthorisationType AUTHORISATION_TYPE = CmsAuthorisationType.CREATED;

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
        when(pisAuthorizationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.of(pisAuthorization));

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongPaymentId() {
        when(pisAuthorizationRepository.findByExternalIdAndAuthorizationType(AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID_WRONG, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        when(pisAuthorizationRepository.findByExternalIdAndAuthorizationType(WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisCommonPaymentService.getAuthorisationScaStatus(PAYMENT_ID, WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationByPaymentIdSuccess() {
        //When
        when(securityDataService.decryptId(PAYMENT_ID)).thenReturn(Optional.of(PAYMENT_ID));
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatus(PAYMENT_ID, TransactionStatus.RCVD)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
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
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatus(PAYMENT_ID_WRONG, TransactionStatus.RCVD)).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentIdAndTransactionStatus(PAYMENT_ID_WRONG, TransactionStatus.RCVD)).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisCommonPaymentService.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG, CmsAuthorisationType.CANCELLED);
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

        when(pisAuthorizationRepository.findByExternalIdAndAuthorizationType(FINALISED_AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(finalisedConsentAuthorization));

        //When
        Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuDataResponse = pisCommonPaymentService.updatePisAuthorisation(FINALISED_AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);

        //Then
        assertTrue(updatePisCommonPaymentPsuDataResponse.isPresent());
        assertNotEquals(updatePisCommonPaymentPsuDataResponse.get().getScaStatus(), expectedScaStatus);
    }

    @Test
    public void updateConsentCancellationAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.STARTED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        PisAuthorization finalisedCancellationAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(expectedScaStatus);

        when(pisAuthorizationRepository.findByExternalIdAndAuthorizationType(FINALISED_CANCELLATION_AUTHORISATION_ID, CmsAuthorisationType.CANCELLED))
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
        PsuIdData psuIdData = buildPsuIdData();
        when(aspspProfileService.getAspspSettings()).thenReturn(getAspspSettings());
        when(pisAuthorizationRepository.save(any(PisAuthorization.class))).thenReturn(pisAuthorization);
        when(pisPaymentDataRepository.findByPaymentIdAndPaymentDataTransactionStatus(PAYMENT_ID, TransactionStatus.RCVD)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        when(pisCommonPaymentConfirmationExpirationService.checkAndUpdatePaymentDataOnConfirmationExpiration(pisPaymentData.getPaymentData())).thenReturn(pisPaymentData.getPaymentData());

        // When
        Optional<CreatePisAuthorisationResponse> actual = pisCommonPaymentService.createAuthorization(PAYMENT_ID, AUTHORISATION_TYPE, psuIdData);

        // Then
        assertTrue(actual.isPresent());
        verify(pisAuthorizationRepository).save(argument.capture());
        assertSame(argument.getValue().getScaStatus(), ScaStatus.STARTED);

        verify(pisAuthorizationRepository).save(failedAuthorisationsArgument.capture());
        List<PisAuthorization> failedAuthorisations = failedAuthorisationsArgument.getValue();
        Set<ScaStatus> scaStatuses = failedAuthorisations.stream()
                                          .map(PisAuthorization::getScaStatus)
                                          .collect(Collectors.toSet());
        assertEquals(scaStatuses.size(), 1);
        assertTrue(scaStatuses.contains(ScaStatus.FAILED));
    }

    @NotNull
    private AspspSettings getAspspSettings() {
        return new AspspSettings(1, false, null, null, false, null, null,
            null, false, null, null, 1, 1, false,
            false, false, false, false, false, 1, null,
            1, 1, null, 1);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("id", "type", "corporate ID", "corporate type");
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(ScaStatus status) {
        UpdatePisCommonPaymentPsuDataRequest request = new UpdatePisCommonPaymentPsuDataRequest();
        request.setAuthorizationId(FINALISED_AUTHORISATION_ID);
        request.setScaStatus(status);
        return request;
    }

    private PisAuthorization buildFinalisedConsentAuthorisation(ScaStatus status) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        pisAuthorization.setScaStatus(status);
        return pisAuthorization;
    }

    private PisCommonPaymentData buildPisCommonPaymentData() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setId(PIS_PAYMENT_DATA_ID);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setAuthorizations(pisAuthorizationList);
        return pisCommonPaymentData;
    }

    private PisAuthorization buildPisAuthorisation(String externalId, CmsAuthorisationType authorisationType) {
        PisAuthorization pisAuthorization = new PisAuthorization();
        pisAuthorization.setExternalId(externalId);
        pisAuthorization.setAuthorizationType(authorisationType);
        pisAuthorization.setScaStatus(SCA_STATUS);
        pisAuthorization.setPaymentData(buildPisCommonPaymentData());
        pisAuthorization.setPsuData(psuDataMapper.mapToPsuData(buildPsuIdData()));
        return pisAuthorization;
    }

    private PisPaymentData buildPaymentData(PisCommonPaymentData pisCommonPaymentData) {
        PisPaymentData paymentData = new PisPaymentData();
        paymentData.setPaymentId(PAYMENT_ID);
        paymentData.setPaymentData(pisCommonPaymentData);
        return paymentData;
    }
}

