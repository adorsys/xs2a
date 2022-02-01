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
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PiisConsentRemoteUrls;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Currency;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentServiceRemote implements PiisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PiisConsentRemoteUrls remotePiisConsentUrls;

    @Override
    public CmsResponse<List<CmsConsent>> getPiisConsentListByAccountIdentifier(@Nullable Currency currency, AccountReferenceSelector accountReferenceSelector) {
        List<CmsConsent> response = Collections.emptyList();

        HttpHeaders headers = new HttpHeaders();
        headers.set("currency", currency == null ? null : currency.toString());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            response = consentRestTemplate.exchange(
                remotePiisConsentUrls.getPiisConsent(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<CmsConsent>>() {
                },
                accountReferenceSelector.getAccountReferenceType().name(),
                accountReferenceSelector.getAccountValue()
            ).getBody();
        } catch (CmsRestException e) {
            log.error("Failed to retrieve piis consent validation data");
        }

        return CmsResponse.<List<CmsConsent>>builder()
                   .payload(response)
                   .build();
    }
}
