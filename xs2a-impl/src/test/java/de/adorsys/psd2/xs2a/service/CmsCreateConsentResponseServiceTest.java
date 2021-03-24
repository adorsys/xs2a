/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsCreateConsentResponseServiceTest {
    private static final String CONSENT_ID = "f2c43cad-6811-4cb6-bfce-31050095ed5d";

    @InjectMocks
    private CmsCreateConsentResponseService service;
    @Mock
    private ConsentServiceEncrypted consentService;
    private static final CmsConsent CMS_CONSENT = new CmsConsent();

    @Test
    void getCmsCreateConsentResponse_WrongChecksumException() throws WrongChecksumException {
        // Given
        when(consentService.createConsent(CMS_CONSENT)).thenThrow(new WrongChecksumException());

        // When
        Optional<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    void getCmsCreateConsentResponse_withError() throws WrongChecksumException {
        // Given
        when(consentService.createConsent(CMS_CONSENT))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Optional<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    void getCmsCreateConsentResponse_success() throws WrongChecksumException {
        // Given
        CmsCreateConsentResponse cmsCreateConsentResponse = new CmsCreateConsentResponse(CONSENT_ID, getCmsConsentWithNotifications());
        when(consentService.createConsent(CMS_CONSENT))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().payload(cmsCreateConsentResponse).build());

        // When
        Optional<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual).isPresent().contains(cmsCreateConsentResponse);
    }

    private CmsConsent getCmsConsentWithNotifications() {
        CmsConsent cmsConsent = new CmsConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppNotificationSupportedModes(Collections.singletonList(NotificationSupportedMode.SCA));
        cmsConsent.setTppInformation(new ConsentTppInformation());
        return cmsConsent;
    }
}
