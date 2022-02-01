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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aPisCommonPaymentService {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    public CreatePisCommonPaymentResponse createCommonPayment(PisPaymentInfo request) {
        CmsResponse<CreatePisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.createCommonPayment(request);

        if (response.hasError()) {
            log.info("Payment ID: [{}]. Pis common payment cannot be created, because can't save to cms DB",
                     request.getPaymentId());
            return null;
        }

        return response.getPayload();
    }

    public Optional<PisCommonPaymentResponse> getPisCommonPaymentById(String paymentId) {
        CmsResponse<PisCommonPaymentResponse> response = pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId);

        if (response.hasError()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    /**
     * Updates multilevelScaRequired and stores changes into database
     *
     * @param paymentId             Payment ID
     * @param multilevelScaRequired new value for boolean multilevel sca required
     * @return true if payment was found and multilevel sca flag was updated, false otherwise
     */
    public boolean updateMultilevelSca(String paymentId, boolean multilevelScaRequired) {
        CmsResponse<Boolean> response = pisCommonPaymentServiceEncrypted.updateMultilevelSca(paymentId, multilevelScaRequired);
        return response.isSuccessful() && response.getPayload();
    }
}
