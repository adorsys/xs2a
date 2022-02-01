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

package de.adorsys.psd2.xs2a.payment.common;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.payment.PaymentServiceResolver;
import de.adorsys.psd2.xs2a.service.payment.cancel.CancelPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreateCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.create.CreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadCommonPaymentService;
import de.adorsys.psd2.xs2a.service.payment.read.ReadPaymentService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadCommonPaymentStatusService;
import de.adorsys.psd2.xs2a.service.payment.status.ReadPaymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceResolverCommon implements PaymentServiceResolver {
    private final CreateCommonPaymentService createCommonPaymentService;
    private final ReadCommonPaymentService readCommonPaymentService;
    private final ReadCommonPaymentStatusService readCommonPaymentStatusService;
    private final CancelPaymentService cancelCommonPaymentService;

    @Override
    public CreatePaymentService getCreatePaymentService(PaymentInitiationParameters paymentInitiationParameters) {
        return createCommonPaymentService;
    }

    @Override
    public ReadPaymentService getReadPaymentService(CommonPaymentData commonPaymentData) {
        return readCommonPaymentService;
    }

    @Override
    public ReadPaymentStatusService getReadPaymentStatusService(PisCommonPaymentResponse pisCommonPaymentResponse) {
        return readCommonPaymentStatusService;
    }

    @Override
    public CancelPaymentService getCancelPaymentService(PisPaymentCancellationRequest paymentCancellationRequest) {
        return cancelCommonPaymentService;
    }
}
