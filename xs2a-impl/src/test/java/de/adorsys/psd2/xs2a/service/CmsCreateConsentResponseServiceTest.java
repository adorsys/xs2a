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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

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
        Xs2aResponse<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual.hasError()).isTrue();
    }

    @Test
    void getCmsCreateConsentResponse_withError() throws WrongChecksumException {
        // Given
        when(consentService.createConsent(CMS_CONSENT))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().error(CmsError.TECHNICAL_ERROR).build());

        // When
        Xs2aResponse<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual.hasError()).isTrue();
    }

    @Test
    void getCmsCreateConsentResponse_success() throws WrongChecksumException {
        // Given
        CmsCreateConsentResponse cmsCreateConsentResponse = new CmsCreateConsentResponse(CONSENT_ID, getCmsConsentWithNotifications());
        when(consentService.createConsent(CMS_CONSENT))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder().payload(cmsCreateConsentResponse).build());

        // When
        Xs2aResponse<CmsCreateConsentResponse> actual = service.getCmsCreateConsentResponse(CMS_CONSENT);

        // Then
        assertThat(actual.isSuccessful()).isTrue();
        assertThat(actual.getPayload()).isEqualTo(cmsCreateConsentResponse);
    }

    private CmsConsent getCmsConsentWithNotifications() {
        CmsConsent cmsConsent = new CmsConsent();
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppNotificationSupportedModes(Collections.singletonList(NotificationSupportedMode.SCA));
        cmsConsent.setTppInformation(new ConsentTppInformation());
        return cmsConsent;
    }
}
