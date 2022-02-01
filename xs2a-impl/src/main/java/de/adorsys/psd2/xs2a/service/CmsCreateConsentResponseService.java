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

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmsCreateConsentResponseService {
    private final ConsentServiceEncrypted consentService;

    public Xs2aResponse<CmsCreateConsentResponse> getCmsCreateConsentResponse(CmsConsent cmsConsent) {
        CmsResponse<CmsCreateConsentResponse> response;
        try {
            response = consentService.createConsent(cmsConsent);
        } catch (WrongChecksumException e) {
            log.info("Consent cannot be created, checksum verification failed");
            return Xs2aResponse.<CmsCreateConsentResponse>builder()
                       .build();
        }

        if (response.hasError()) {
            log.info("Consent cannot be created, because can't save to cms DB");
            return Xs2aResponse.<CmsCreateConsentResponse>builder()
                       .build();
        }

        return Xs2aResponse.<CmsCreateConsentResponse>builder()
                   .payload(response.getPayload())
                   .build();
    }
}
