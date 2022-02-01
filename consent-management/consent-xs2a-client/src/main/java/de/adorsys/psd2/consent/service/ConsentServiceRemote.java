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
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.ais.ConsentStatusResponse;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.ConsentRemoteUrls;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsentServiceRemote implements ConsentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final ConsentRemoteUrls consentRemoteUrls;

    @Override
    public CmsResponse<CmsCreateConsentResponse> createConsent(CmsConsent cmsConsent) {
        try {
            ResponseEntity<CmsCreateConsentResponse> restResponse = consentRestTemplate.postForEntity(consentRemoteUrls.createConsent(), cmsConsent, CmsCreateConsentResponse.class);
            return CmsResponse.<CmsCreateConsentResponse>builder()
                       .payload(restResponse.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't create new consent, HTTP response status: {}", cmsRestException.getHttpStatus());
        }
        return CmsResponse.<CmsCreateConsentResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<ConsentStatus> getConsentStatusById(String consentId) {
        try {
            ConsentStatusResponse response = consentRestTemplate.getForEntity(consentRemoteUrls.getConsentStatusById(), ConsentStatusResponse.class, consentId).getBody();
            if (response != null) {
                return CmsResponse.<ConsentStatus>builder()
                           .payload(response.getConsentStatus())
                           .build();
            }

            log.info("Couldn't get consent status by consent ID {}", consentId);
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get consent status by consent ID {}, HTTP response status: {}",
                     consentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<ConsentStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) {
        try {
            consentRestTemplate.put(consentRemoteUrls.updateConsentStatusById(), null, consentId, status);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update consent status by consent ID {}, HTTP response status: {}",
                     consentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<CmsConsent> getConsentById(String consentId) {
        try {
            ResponseEntity<CmsConsent> accountConsent = consentRestTemplate.getForEntity(consentRemoteUrls.getConsentById(), CmsConsent.class, consentId);
            return CmsResponse.<CmsConsent>builder()
                       .payload(accountConsent.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get consent by consent ID {}, HTTP response status: {}",
                     consentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<CmsConsent>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        try {
            consentRestTemplate.delete(consentRemoteUrls.findAndTerminateOldConsentsByNewConsentId(), newConsentId);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't terminate old consents by new consent ID {}, HTTP response status: {}",
                     newConsentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> findAndTerminateOldConsents(String newConsentId, TerminateOldConsentsRequest request) {
        try {
            consentRestTemplate.put(consentRemoteUrls.findAndTerminateOldConsents(), request, newConsentId);

            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't terminate old consents by new consent ID {}, HTTP response status: {}",
                     newConsentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String consentId) {
        try {
            ResponseEntity<List<PsuIdData>> response =
                consentRestTemplate.exchange(consentRemoteUrls.getPsuDataByConsentId(),
                                             HttpMethod.GET,
                                             null,
                                             new ParameterizedTypeReference<List<PsuIdData>>() {
                                             },
                                             consentId);
            return CmsResponse.<List<PsuIdData>>builder()
                       .payload(response.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't get PSU Data by consent ID {}, HTTP response status: {}",
                     consentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<List<PsuIdData>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateMultilevelScaRequired(String encryptedConsentId, boolean multilevelScaRequired) {
        try {
            Boolean updateResponse = consentRestTemplate.exchange(consentRemoteUrls.updateMultilevelScaRequired(),
                                                                  HttpMethod.PUT, null, Boolean.class, encryptedConsentId, multilevelScaRequired)
                                         .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(updateResponse)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update multilevel SCA required by consent ID {}, HTTP response status: {}",
                     encryptedConsentId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }
}
