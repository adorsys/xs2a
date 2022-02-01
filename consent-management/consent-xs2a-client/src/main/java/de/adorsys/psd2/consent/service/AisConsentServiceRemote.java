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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.UpdateAisConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.core.data.AccountAccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentServiceRemote implements AisConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public CmsResponse<CmsResponse.VoidResponse> checkConsentAndSaveActionLog(AisConsentActionRequest request) {
        try {
            consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), request, Void.class);
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't check consent and save action log, HTTP response status: {}", cmsRestException.getHttpStatus());

            return CmsResponse.<CmsResponse.VoidResponse>builder()
                       .error(cmsRestException.getCmsError())
                       .build();
        }

        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    public CmsResponse<CmsConsent> updateAspspAccountAccess(String consentId, AccountAccess request) {
        try {
            UpdateAisConsentResponse response = consentRestTemplate.exchange(remoteAisConsentUrls.updateAisAccountAccess(), HttpMethod.PUT,
                                                                             new HttpEntity<>(request), UpdateAisConsentResponse.class, consentId).getBody();
            if (response != null) {
                return CmsResponse.<CmsConsent>builder()
                           .payload(response.getAisConsent())
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update ASPSP account access, HTTP response status: {}", cmsRestException.getHttpStatus());

            return CmsResponse.<CmsConsent>builder()
                       .error(cmsRestException.getCmsError())
                       .build();
        }

        return CmsResponse.<CmsConsent>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }
}
