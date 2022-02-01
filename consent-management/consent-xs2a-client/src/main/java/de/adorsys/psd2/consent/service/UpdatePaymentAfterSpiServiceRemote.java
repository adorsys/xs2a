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
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PisPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdatePaymentAfterSpiServiceRemote implements UpdatePaymentAfterSpiServiceEncrypted {
    public static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found, id: {}";

    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisPaymentRemoteUrls pisPaymentRemoteUrls;

    @Override
    public CmsResponse<Boolean> updatePaymentStatus(@NotNull String encryptedPaymentId, @NotNull TransactionStatus status) {
        try {
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updatePaymentStatus(), HttpMethod.PUT, null, Void.class, encryptedPaymentId, status.name());
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException e) {
            log.error(PAYMENT_NOT_FOUND_MESSAGE, encryptedPaymentId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateInternalPaymentStatus(@NotNull String encryptedPaymentId, @NotNull InternalPaymentStatus status) {
        try {
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updateInternalPaymentStatus(), HttpMethod.PUT, null, Void.class, encryptedPaymentId, status.name());
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException e) {
            log.error(PAYMENT_NOT_FOUND_MESSAGE, encryptedPaymentId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updatePaymentCancellationTppRedirectUri(@NotNull String encryptedPaymentId, @NotNull TppRedirectUri tppRedirectUri) {
        try {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("tpp-redirect-uri", StringUtils.defaultIfBlank(tppRedirectUri.getUri(), ""));
            headers.add("tpp-nok-redirect-uri", StringUtils.defaultIfBlank(tppRedirectUri.getNokUri(), ""));
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updatePaymentCancellationRedirectURIs(), HttpMethod.PUT, new HttpEntity<>(headers), Void.class, encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException e) {
            log.error(PAYMENT_NOT_FOUND_MESSAGE, encryptedPaymentId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updatePaymentCancellationInternalRequestId(@NotNull String encryptedPaymentId, @NotNull String internalRequestId) {
        try {
            consentRestTemplate.exchange(pisPaymentRemoteUrls.updatePaymentCancellationInternalRequestId(), HttpMethod.PUT, null, Void.class, encryptedPaymentId, internalRequestId);
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException e) {
            log.error(PAYMENT_NOT_FOUND_MESSAGE, encryptedPaymentId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }
}
