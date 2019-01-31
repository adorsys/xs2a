/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.pis.proto.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aPisCommonPaymentService {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;

    /**
     * Creates PIS consent
     *
     * @param parameters Payment request parameters to get needed payment info
     * @param tppInfo    information about TPP
     * @return String consentId
     */
    // TODO refactoring for orElse(null)
    public CreatePisCommonPaymentResponse createCommonPayment(PaymentInitiationParameters parameters, TppInfo tppInfo) {
        return createCommonPayment(parameters, tppInfo, null);
    }

    public CreatePisCommonPaymentResponse createCommonPayment(PisPaymentInfo request) {
        return pisCommonPaymentServiceEncrypted.createCommonPayment(request)
                   .orElse(null);
    }

    public CreatePisCommonPaymentResponse createCommonPayment(PaymentInitiationParameters parameters, TppInfo tppInfo, byte[] paymentData) {
        PisPaymentInfo request = new PisPaymentInfo();
        request.setPaymentProduct(parameters.getPaymentProduct());
        request.setPaymentType(parameters.getPaymentType());
        request.setTransactionStatus(TransactionStatus.RCVD);
        request.setPaymentData(paymentData);
        request.setTppInfo(tppInfo);
        request.setPsuDataList(Collections.singletonList(parameters.getPsuData()));
        return pisCommonPaymentServiceEncrypted.createCommonPayment(request)
                   .orElse(null);
    }

    public Optional<PisCommonPaymentResponse> getPisCommonPaymentById(String paymentId) {
        return pisCommonPaymentServiceEncrypted.getCommonPaymentById(paymentId);
    }

    public void updateSinglePaymentInCommonPayment(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsSinglePisCommonPaymentRequest(singlePayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }

    public void updatePeriodicPaymentInCommonPayment(PeriodicPayment periodicPayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsPeriodicPisCommonPaymentRequest(periodicPayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }

    public void updateBulkPaymentInCommonPayment(BulkPayment bulkPayment, PaymentInitiationParameters paymentInitiationParameters, String paymentId) {
        PisCommonPaymentRequest pisCommonPaymentRequest = xs2aToCmsPisCommonPaymentRequestMapper.mapToCmsBulkPisCommonPaymentRequest(bulkPayment, paymentInitiationParameters.getPaymentProduct());
        pisCommonPaymentServiceEncrypted.updateCommonPayment(pisCommonPaymentRequest, paymentId);
    }
}
