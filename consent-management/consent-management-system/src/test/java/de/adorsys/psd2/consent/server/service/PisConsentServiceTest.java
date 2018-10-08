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

import de.adorsys.psd2.consent.api.UpdateConsentAspspDataRequest;
import de.adorsys.psd2.consent.server.domain.payment.PisConsent;
import de.adorsys.psd2.consent.server.repository.PisConsentRepository;
import de.adorsys.psd2.consent.server.service.mapper.PisConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EnumSet;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsConsentStatus.RECEIVED;
import static de.adorsys.psd2.consent.api.CmsConsentStatus.VALID;
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

    private PisConsent pisConsent;
    private final long CONSENT_ID = 1;
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";

    @Before
    public void setUp() {
        pisConsent = buildConsent();
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
}
