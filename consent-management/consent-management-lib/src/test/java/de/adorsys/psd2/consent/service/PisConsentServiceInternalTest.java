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


import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.domain.AspspConsentDataEntity;
import de.adorsys.psd2.consent.domain.payment.PisConsent;
import de.adorsys.psd2.consent.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.service.security.EncryptedData;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisConsentServiceInternalTest {

    @InjectMocks
    private PisConsentServiceInternal pisConsentService;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisConsentAuthorizationRepository pisConsentAuthorizationRepository;
    @Mock
    SecurityDataService securityDataService;

    private PisConsent pisConsent;
    private final long CONSENT_ID = 1;
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private final String paymentId = "5bbde955ca10e8e4035a10c2";
    private final String paymentIdWrong = "5bbdcb28ca10e8e14a41b12f";
    private static final byte[] ENCRYPTED_CONSENT_DATA = "test data".getBytes();
    private PisPaymentData pisPaymentData;
    private List<PisConsentAuthorization> pisConsentAuthorizationList = new ArrayList();
    private CmsAspspConsentDataBase64 cmsAspspConsentDataBase64;
    private static final String FINALISED_AUTHORISATION_ID = "9b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String FINALISED_CANCELLATION_AUTHORISATION_ID = "2a112130-6a96-4941-a220-2da8a4af2c65";

    @Before
    public void setUp() {
        cmsAspspConsentDataBase64 = buildUpdateBlobRequest();
        pisConsentAuthorizationList.add(buildPisConsentAuthorisation("906a08bc-8347-4f08-8c24-eda17b1f4c57"));
        pisConsent = buildConsent();
        pisPaymentData = buildPaymentData(pisConsent);
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID));
        when(securityDataService.decryptId(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(Optional.of(EXTERNAL_CONSENT_ID_NOT_EXIST));
        when(securityDataService.encryptConsentData(EXTERNAL_CONSENT_ID, cmsAspspConsentDataBase64.getAspspConsentDataBase64()))
            .thenReturn(Optional.of(new EncryptedData(ENCRYPTED_CONSENT_DATA)));
    }

    @Test
    public void getAuthorisationByPaymentIdSuccess() {
        //When
        when(securityDataService.decryptId(paymentId)).thenReturn(Optional.of(paymentId));
        when(pisPaymentDataRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(Collections.singletonList(pisPaymentData)));
        //Then
        Optional<List<String>> authorizationByPaymentId = pisConsentService.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CANCELLED);
        //Assert
        //noinspection OptionalGetWithoutIsPresent
        assertTrue(authorizationByPaymentId.isPresent());
        assertEquals(authorizationByPaymentId.get().size(), pisConsentAuthorizationList.size());
        assertEquals(authorizationByPaymentId.get().get(0), pisConsentAuthorizationList.get(0).getExternalId());
    }

    @Test
    public void getAuthorisationByPaymentIdWrongPaymentId() {
        //When
        when(securityDataService.decryptId(paymentIdWrong)).thenReturn(Optional.empty());
        when(pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentIdWrong, RECEIVED)).thenReturn(Optional.empty());
        //Then
        Optional<List<String>> authorizationByPaymentId = pisConsentService.getAuthorisationsByPaymentId(paymentIdWrong, CmsAuthorisationType.CANCELLED);
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

    private CmsAspspConsentDataBase64 buildUpdateBlobRequest() {
        return new CmsAspspConsentDataBase64("encryptedId",
            Base64.getEncoder().encodeToString("decrypted consent data".getBytes()));
    }

    private PisConsentAuthorization buildPisConsentAuthorisation(String externalId) {
        PisConsentAuthorization pisConsentAuthorization = new PisConsentAuthorization();
        pisConsentAuthorization.setExternalId(externalId);
        return pisConsentAuthorization;
    }

    private PisPaymentData buildPaymentData(PisConsent pisConsent) {
        PisPaymentData paymentData = new PisPaymentData();
        paymentData.setPaymentId(paymentId);
        paymentData.setConsent(pisConsent);
        return paymentData;
    }

    private AspspConsentDataEntity getAspspConsentData() {
        AspspConsentDataEntity consentData = new AspspConsentDataEntity();
        consentData.setConsentId(EXTERNAL_CONSENT_ID);
        consentData.setData(ENCRYPTED_CONSENT_DATA);
        return consentData;
    }


}
