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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.PisCommonPaymentApi;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentDataStatusResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PisCommonPaymentController implements PisCommonPaymentApi {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Override
    public ResponseEntity<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        CmsResponse<CreatePisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.createCommonPayment(request);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<PisCommonPaymentDataStatusResponse> getPisCommonPaymentStatusById(String paymentId) {
        CmsResponse<TransactionStatus> response = pisCommonPaymentServiceEncrypted.getPisCommonPaymentStatusById(paymentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(new PisCommonPaymentDataStatusResponse(response.getPayload()), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        CmsResponse<PisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId);

        if (response.hasError()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateCommonPaymentStatus(String paymentId, String status) {
        TransactionStatus transactionStatus = TransactionStatus.getByValue(status);
        if (transactionStatus == null) {
            log.error("Invalid transaction status: [{}] for payment-ID [{}]", status, paymentId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        CmsResponse<Boolean> response = pisCommonPaymentServiceEncrypted.updateCommonPaymentStatusById(paymentId, transactionStatus);
        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Boolean> updateMultilevelScaRequired(String paymentId, boolean multilevelSca) {
        CmsResponse<Boolean> response = pisCommonPaymentServiceEncrypted.updateMultilevelSca(paymentId, multilevelSca);

        if (response.isSuccessful() && BooleanUtils.isTrue(response.getPayload())) {
            return new ResponseEntity<>(response.getPayload(), HttpStatus.OK);

        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
