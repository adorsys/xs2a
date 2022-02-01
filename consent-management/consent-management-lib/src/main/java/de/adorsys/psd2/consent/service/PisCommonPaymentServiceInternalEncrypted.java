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
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PisCommonPaymentServiceInternalEncrypted implements PisCommonPaymentServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final PisCommonPaymentService pisCommonPaymentService;

    @Override
    @Transactional
    public CmsResponse<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        CmsResponse<CreatePisCommonPaymentResponse> paymentResponse = pisCommonPaymentService.createCommonPayment(request);

        if (paymentResponse.hasError()) {
            return paymentResponse;
        }

        CreatePisCommonPaymentResponse payment = paymentResponse.getPayload();
        Optional<String> encryptIdOptional = securityDataService.encryptId(payment.getPaymentId());

        if (encryptIdOptional.isEmpty()) {
            log.info("Payment ID: [{}]. Create common payment failed, couldn't encrypt payment id", payment.getPaymentId());
            return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                   .payload(new CreatePisCommonPaymentResponse(encryptIdOptional.get(), payment.getTppNotificationContentPreferred()))
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<TransactionStatus> getPisCommonPaymentStatusById(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment status by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<TransactionStatus>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getPisCommonPaymentStatusById(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<PisCommonPaymentResponse> getCommonPaymentById(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<PisCommonPaymentResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getCommonPaymentById(decryptIdOptional.get());
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateCommonPaymentStatusById(String encryptedPaymentId, TransactionStatus status) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Get common payment status by ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.updateCommonPaymentStatusById(decryptIdOptional.get(), status);
    }

    @Override
    public CmsResponse<String> getDecryptedId(String encryptedId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Couldn't decrypt consent id", encryptedId);
            return CmsResponse.<String>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }
        return CmsResponse.<String>builder()
                   .payload(decryptIdOptional.get())
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateMultilevelSca(String encryptedPaymentId, boolean multilevelScaRequired) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Update payment multilevel SCA failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.updateMultilevelSca(decryptIdOptional.get(), multilevelScaRequired);
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataListByPaymentId(String encryptedPaymentId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedPaymentId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Payment ID: [{}]. Get PSU data list by payment ID failed, couldn't decrypt consent id",
                     encryptedPaymentId);
            return CmsResponse.<List<PsuIdData>>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return pisCommonPaymentService.getPsuDataListByPaymentId(decryptIdOptional.get());
    }
}
