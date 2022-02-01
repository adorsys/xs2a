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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.CmsAspspPisTransactionApi;
import de.adorsys.psd2.consent.aspsp.api.pis.AspspPaymentService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CmsAspspPisTransactionController implements CmsAspspPisTransactionApi {
    private final AspspPaymentService aspspPaymentService;

    @Override
    public ResponseEntity<Void> updatePaymentStatus(String paymentId, String status, String instanceId) {
        TransactionStatus transactionStatus;
        try {
            transactionStatus = TransactionStatus.valueOf(status);
        } catch (IllegalArgumentException illegalArgumentException) {
            return ResponseEntity.badRequest().build();
        }
        return aspspPaymentService.updatePaymentStatus(paymentId, transactionStatus, instanceId)
            ? ResponseEntity.ok().build()
            : ResponseEntity.badRequest().build();
    }
}


