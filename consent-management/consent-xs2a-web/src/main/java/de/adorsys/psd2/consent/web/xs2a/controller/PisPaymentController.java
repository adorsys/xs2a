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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.PisPaymentApi;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.UpdatePaymentAfterSpiServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PisPaymentController implements PisPaymentApi {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentService;
    private final UpdatePaymentAfterSpiServiceEncrypted updatePaymentStatusAfterSpiService;

    @Override
    public ResponseEntity<String> getPaymentIdByEncryptedString(String encryptedId) {
        CmsResponse<String> response = pisCommonPaymentService.getDecryptedId(encryptedId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updatePaymentStatusAfterSpiService(String paymentId, String status) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status));

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Void> updatePaymentCancellationTppRedirectUri(String paymentId, String tpPRedirectURI, String tpPNokRedirectURI) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationTppRedirectUri(
            paymentId,
            new TppRedirectUri(
                StringUtils.defaultIfBlank(tpPRedirectURI, ""),
                StringUtils.defaultIfBlank(tpPNokRedirectURI, "")));
        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Void> updatePaymentCancellationInternalRequestId(String paymentId, String internalRequestId) {
        CmsResponse<Boolean> response = updatePaymentStatusAfterSpiService.updatePaymentCancellationInternalRequestId(paymentId, internalRequestId);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
