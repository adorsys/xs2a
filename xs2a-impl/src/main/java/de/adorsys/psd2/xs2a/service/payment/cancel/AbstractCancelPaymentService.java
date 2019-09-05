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

package de.adorsys.psd2.xs2a.service.payment.cancel;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_404;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_404;

@RequiredArgsConstructor
public abstract class AbstractCancelPaymentService implements CancelPaymentService {
    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Payment not found"; //TODO: move to bundle https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/791

    private final de.adorsys.psd2.xs2a.service.payment.CancelPaymentService cancelPaymentService;

    @Override
    public ResponseObject<CancelPaymentResponse> cancelPayment(CommonPaymentData commonPaymentData, PisPaymentCancellationRequest paymentCancellationRequest) {
        List<PisPayment> pisPayments = getPisPayments(commonPaymentData);
        if (!isCommonPayment() && CollectionUtils.isEmpty(pisPayments)) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        Optional<? extends SpiPayment> spiPaymentOptional = createSpiPayment(pisPayments, commonPaymentData);
        if (!spiPaymentOptional.isPresent()) {
            return ResponseObject.<CancelPaymentResponse>builder()
                       .fail(PIS_404, of(RESOURCE_UNKNOWN_404, PAYMENT_NOT_FOUND_MESSAGE))
                       .build();
        }

        return this.cancelPaymentService.initiatePaymentCancellation(spiPaymentOptional.get(),
                                                                     paymentCancellationRequest.getEncryptedPaymentId(),
                                                                     paymentCancellationRequest.getTppExplicitAuthorisationPreferred(),
                                                                     paymentCancellationRequest.getTppRedirectUri());
    }

    protected abstract Optional<? extends SpiPayment> createSpiPayment(List<PisPayment> pisPayments, CommonPaymentData commonPaymentData);

    protected boolean isCommonPayment(){
        return false;
    }

    private List<PisPayment> getPisPayments(CommonPaymentData commonPaymentData) {
        List<PisPayment> pisPayments = Optional.of(commonPaymentData)
                                           .map(CommonPaymentData::getPayments)
                                           .orElseGet(Collections::emptyList);

        pisPayments.forEach(pmt -> {
            pmt.setPaymentId(commonPaymentData.getExternalId());
            pmt.setTransactionStatus(commonPaymentData.getTransactionStatus());
            pmt.setPsuDataList(commonPaymentData.getPsuData());
            pmt.setStatusChangeTimestamp(commonPaymentData.getStatusChangeTimestamp());
        });

        return pisPayments;
    }
}
