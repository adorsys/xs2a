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

package de.adorsys.psd2.consent.server.service;

import de.adorsys.psd2.consent.api.CmsAuthorizationType;
import de.adorsys.psd2.consent.api.UpdateConsentAspspDataRequest;
import de.adorsys.psd2.consent.server.domain.payment.PisConsent;
import de.adorsys.psd2.consent.server.domain.payment.PisConsentAuthorization;
import de.adorsys.psd2.consent.server.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.server.repository.PisConsentAuthorizationRepository;
import de.adorsys.psd2.consent.server.repository.PisConsentRepository;
import de.adorsys.psd2.consent.server.repository.PisPaymentDataRepository;
import de.adorsys.psd2.consent.server.service.mapper.PisConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsConsentStatus.RECEIVED;
import static de.adorsys.psd2.consent.api.CmsConsentStatus.VALID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisConsentServiceTest {

    @InjectMocks
    private PisConsentService pisConsentService;
    @Mock
    private PisConsentMapper consentMapper;
    @Mock
    private PisConsentRepository pisConsentRepository;
    @Mock
    private PisPaymentDataRepository pisPaymentDataRepository;
    @Mock
    private PisConsentAuthorizationRepository pisConsentAuthorizationRepository;

    private PisConsent pisConsent;
    private final long CONSENT_ID = 1;
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private final String paymentId = "5bbde955ca10e8e4035a10c2";
     private final String paymentIdWrong = "5bbdcb28ca10e8e14a41b12f";
    private PisPaymentData pisPaymentData;
    private List<PisConsentAuthorization> pisConsentAuthorizationList = new ArrayList();

    @Before
    public void setUp() {
        pisConsent = buildConsent();
        pisPaymentData = buildPaymentData(pisConsent);
        pisConsentAuthorizationList.add(buildPisConsentAuthorization("906a08bc-8347-4f08-8c24-eda17b1f4c57"));
    }

    @Test
    public void updateAspspDataById() {

        // When
        when(pisConsentRepository.findByExternalIdAndConsentStatusIn(EXTERNAL_CONSENT_ID, EnumSet.of(RECEIVED, VALID))).thenReturn(Optional.ofNullable(pisConsent));
        when(pisConsentRepository.findByExternalIdAndConsentStatusIn(EXTERNAL_CONSENT_ID_NOT_EXIST, EnumSet.of(RECEIVED, VALID))).thenReturn(Optional.empty());
        when(pisConsentRepository.save(any(PisConsent.class))).thenReturn(pisConsent);

        // Then
        UpdateConsentAspspDataRequest request = this.buildUpdateBlobRequest();
        Optional<String> consentId = pisConsentService.updateAspspConsentData(EXTERNAL_CONSENT_ID, request);
        // Assert
        assertTrue(consentId.isPresent());

        //Then
        Optional<String> consentId_notExists = pisConsentService.updateAspspConsentData(EXTERNAL_CONSENT_ID_NOT_EXIST, request);
        // Assert
        assertFalse(consentId_notExists.isPresent());
    }

    @Test
    public void getAuthorizationByPaymentIdSuccess(){
        //When
        when(pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentId, RECEIVED)).thenReturn(Optional.of(pisPaymentData));
        when(pisConsentAuthorizationRepository.findByConsentIdAndAuthorizationType(CONSENT_ID, CmsAuthorizationType.CANCELLED)).thenReturn(Optional.of(pisConsentAuthorizationList));
        //Then
        Optional<String> authorizationByPaymentId = pisConsentService.getAuthorizationByPaymentId(paymentId);
        //Assert
        assertEquals(authorizationByPaymentId.get(), pisConsentAuthorizationList.get(0).getExternalId());
    }

    @Test
    public void getAuthorizationByPaymentIdWrongPaymentId(){
        //When
        when(pisPaymentDataRepository.findByPaymentIdAndConsent_ConsentStatus(paymentIdWrong, RECEIVED)).thenReturn(Optional.empty());
        //Then
        Optional<String> authorizationByPaymentId = pisConsentService.getAuthorizationByPaymentId(paymentIdWrong);
        //Assert
        assertFalse(authorizationByPaymentId.isPresent());
    }


    private PisConsent buildConsent() {
        PisConsent pisConsent = new PisConsent();
        pisConsent.setId(CONSENT_ID);
        pisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        return pisConsent;
    }

    private UpdateConsentAspspDataRequest buildUpdateBlobRequest() {
        UpdateConsentAspspDataRequest request = new UpdateConsentAspspDataRequest();
        request.setAspspConsentDataBase64("zdxcvvzzzxcvzzzz");
        return request;
    }

    private PisConsentAuthorization buildPisConsentAuthorization(String externalId) {
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


}
