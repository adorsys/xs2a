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


import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisConsentServiceInternalTest {
    private static final long CONSENT_ID = 1;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String PAYMENT_ID_WRONG = "5bbdcb28ca10e8e14a41b12f";
    private static final String FINALISED_AUTHORISATION_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_CANCELLATION_AUTHORISATION_ID = "2a112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;

    @InjectMocks
    private PisConsentServiceInternal pisConsentService;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private PisConsentAuthorizationRepository pisConsentAuthorizationRepository;

    private PisConsent pisConsent;
    private PisPaymentData pisPaymentData;
    private List<PisConsentAuthorization> pisConsentAuthorizationList = new ArrayList<>();

    @Before
    public void setUp() {
        pisConsent = buildConsent();
        pisPaymentData = buildPaymentData(pisConsent);
        pisConsentAuthorizationList.add(buildPisConsentAuthorisation(EXTERNAL_CONSENT_ID, CmsAuthorisationType.CANCELLED));
        pisConsentAuthorizationList.add(buildPisConsentAuthorisation(AUTHORISATION_ID, CmsAuthorisationType.CREATED));
    }

    @Test
    public void getAuthorisationByPaymentIdSuccess() {
        //When
        when(pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(PAYMENT_ID, RECEIVED)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        //Then
        Optional<List<String>> authorizationByPaymentId = pisConsentService.getAuthorisationsByPaymentId(PAYMENT_ID, CmsAuthorisationType.CANCELLED);
        //Assert
        assertTrue(authorizationByPaymentId.isPresent());
        assertEquals(1, authorizationByPaymentId.get().size());
        assertEquals(pisConsentAuthorizationList.get(0).getExternalId(), authorizationByPaymentId.get().get(0));
    }

    @Test
    public void getAuthorisationByPaymentIdWrongPaymentId() {
        //When
        when(pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(PAYMENT_ID_WRONG, RECEIVED)).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(PAYMENT_ID_WRONG, RECEIVED)).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisConsentService.getAuthorisationsByPaymentId(PAYMENT_ID_WRONG, CmsAuthorisationType.CANCELLED);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }

    @Test
    public void updateConsentAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.STARTED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        UpdatePisConsentPsuDataRequest updatePisConsentPsuDataRequest = buildUpdatePisConsentPsuDataRequest(expectedScaStatus);
        PisConsentAuthorization finalisedConsentAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);

        when(pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(FINALISED_AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .thenReturn(Optional.of(finalisedConsentAuthorization));

        //When
        Optional<UpdatePisConsentPsuDataResponse> updatePisConsentPsuDataResponse = pisConsentService.updateConsentAuthorisation(FINALISED_AUTHORISATION_ID, updatePisConsentPsuDataRequest);

        //Then
        assertTrue(updatePisConsentPsuDataResponse.isPresent());
        assertNotEquals(updatePisConsentPsuDataResponse.get().getScaStatus(), expectedScaStatus);
    }

    @Test
    public void updateConsentCancellationAuthorisation_FinalisedStatus_Fail() {
        //Given
        ScaStatus expectedScaStatus = ScaStatus.STARTED;
        ScaStatus actualScaStatus = ScaStatus.FINALISED;

        PisConsentAuthorization finalisedCancellationAuthorization = buildFinalisedConsentAuthorisation(actualScaStatus);
        UpdatePisConsentPsuDataRequest updatePisConsentPsuDataRequest = buildUpdatePisConsentPsuDataRequest(expectedScaStatus);

        when(pisConsentAuthorizationRepository.findByExternalIdAndAuthorizationType(FINALISED_CANCELLATION_AUTHORISATION_ID, CmsAuthorisationType.CANCELLED))
            .thenReturn(Optional.of(finalisedCancellationAuthorization));

        //When
        Optional<UpdatePisConsentPsuDataResponse> updatePisConsentPsuDataResponse = pisConsentService.updateConsentCancellationAuthorisation(FINALISED_CANCELLATION_AUTHORISATION_ID, updatePisConsentPsuDataRequest);

        //Then
        assertTrue(updatePisConsentPsuDataResponse.isPresent());
        assertNotEquals(updatePisConsentPsuDataResponse.get().getScaStatus(), expectedScaStatus);

    }

    @Test
    public void getAuthorisationScaStatus_success() {
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));

        // When
        Optional<ScaStatus> actual = pisConsentService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(SCA_STATUS, actual.get());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongPaymentId() {
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG)).thenReturn(Optional.empty());
        when(pisCommonPaymentDataRepository.findByPaymentId(PAYMENT_ID_WRONG)).thenReturn(Optional.empty());

        // When
        Optional<ScaStatus> actual = pisConsentService.getAuthorisationScaStatus(PAYMENT_ID_WRONG, AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    public void getAuthorisationScaStatus_failure_wrongAuthorisationId() {
        when(pisPaymentDataRepository.findByPaymentId(PAYMENT_ID)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));

        // When
        Optional<ScaStatus> actual = pisConsentService.getAuthorisationScaStatus(PAYMENT_ID, WRONG_AUTHORISATION_ID, CmsAuthorisationType.CREATED);

        // Then
        assertFalse(actual.isPresent());
    }

    private UpdatePisConsentPsuDataRequest buildUpdatePisConsentPsuDataRequest(ScaStatus status) {
        UpdatePisConsentPsuDataRequest request = new UpdatePisConsentPsuDataRequest();
        request.setAuthorizationId(FINALISED_AUTHORISATION_ID);
        request.setScaStatus(status);
        return request;
    }

    private PisConsentAuthorization buildFinalisedConsentAuthorisation(ScaStatus status) {
        PisConsentAuthorization pisConsentAuthorization = new PisConsentAuthorization();
        pisConsentAuthorization.setExternalId(FINALISED_AUTHORISATION_ID);
        pisConsentAuthorization.setScaStatus(status);
        return pisConsentAuthorization;
    }

    private PisConsent buildConsent() {
        PisConsent pisConsent = new PisConsent();
        pisConsent.setId(CONSENT_ID);
        pisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        pisConsent.setConsentStatus(RECEIVED);
        pisConsent.setAuthorizations(pisConsentAuthorizationList);
        return pisConsent;
    }

    private PisConsentAuthorization buildPisConsentAuthorisation(String externalId, CmsAuthorisationType authorisationType) {
        PisConsentAuthorization pisConsentAuthorisation = new PisConsentAuthorization();
        pisConsentAuthorisation.setExternalId(externalId);
        pisConsentAuthorisation.setAuthorizationType(authorisationType);
        pisConsentAuthorisation.setScaStatus(SCA_STATUS);
        return pisConsentAuthorisation;
    }

    private PisPaymentData buildPaymentData(PisConsent pisConsent) {
        PisPaymentData paymentData = new PisPaymentData();
        paymentData.setPaymentId(PAYMENT_ID);
        paymentData.setConsent(pisConsent);
        return paymentData;
    }

}
