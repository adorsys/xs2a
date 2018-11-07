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

import de.adorsys.psd2.consent.api.piis.CmsPiisValidationInfo;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.config.PiisConsentRemoteUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
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
    public List<CmsPiisValidationInfo> getPiisConsentListByAccountIdentifier(Currency currency, String accountIdentifierName, String accountIdentifier) {
        List<CmsPiisValidationInfo> response = Collections.emptyList();

        try {
            response = consentRestTemplate.exchange(
                remotePiisConsentUrls.getPiisConsent(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CmsPiisValidationInfo>>() {
                },
                currency.toString(),
                accountIdentifierName,
                accountIdentifier
            ).getBody();
        } catch (RuntimeException e) {
            log.error("Failed to retrieve piis consent validation data, message: {}, stackTrace: {}.", e.getMessage(), e);
        }

        return response;
    }
}
