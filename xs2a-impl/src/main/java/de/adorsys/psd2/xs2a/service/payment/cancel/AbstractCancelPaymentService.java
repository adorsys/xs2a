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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_404;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404_NO_PAYMENT;

@RequiredArgsConstructor
public abstract class AbstractCancelPaymentService implements CancelPaymentService {

    private final de.adorsys.psd2.xs2a.service.payment.CancelPaymentService cancelPaymentService;

    @Override
    public ResponseObject<CancelPaymentResponse> cancelPayment(CommonPaymentData commonPaymentData, PisPaymentCancellationRequest paymentCancellationRequest) {
        Optional<? extends SpiPayment> spiPaymentOptional = createSpiPayment(commonPaymentData);
        if (spiPaymentOptional.isEmpty()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404_NO_PAYMENT))
                       .build();
        }

        return this.cancelPaymentService.initiatePaymentCancellation(spiPaymentOptional.get(),
                                                                     paymentCancellationRequest.getEncryptedPaymentId(),
                                                                     paymentCancellationRequest.getTppExplicitAuthorisationPreferred(),
                                                                     paymentCancellationRequest.getTppRedirectUri());
    }

    protected abstract Optional<SpiPayment> createSpiPayment(CommonPaymentData commonPaymentData);
}
