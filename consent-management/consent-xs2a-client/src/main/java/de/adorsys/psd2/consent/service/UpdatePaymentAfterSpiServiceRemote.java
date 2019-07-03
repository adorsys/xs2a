/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PisPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentAfterSpiServiceRemote implements UpdatePaymentAfterSpiServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisPaymentRemoteUrls pisPaymentRemoteUrls;

    @Override
    public boolean updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        try {
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updatePaymentStatus(), HttpMethod.PUT, null, Void.class, encryptedPaymentId, status.name());
            return true;
        } catch (CmsRestException e) {
            log.error("Payment not found, id: {}", encryptedPaymentId);
            return false;
        }
    }

    @Override
    public boolean updatePaymentCancellationTppRedirectUri(@NotNull String encryptedPaymentId, @NotNull TppRedirectUri tppRedirectUri) {
        try {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("tpp-redirect-uri", tppRedirectUri.getUri());
            headers.add("tpp-nok-redirect-uri", tppRedirectUri.getNokUri());
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updatePaymentCancellationRedirectURIs(), HttpMethod.PUT, new HttpEntity<>(headers), Void.class, encryptedPaymentId);
            return true;
        } catch (CmsRestException e) {
            log.error("Payment not found, id: {}", encryptedPaymentId);
            return false;
        }
    }
}
