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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.consent.pis.PisConsentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class RedirectApproachPaymentService implements ScaPaymentService {
    @Autowired
    private PisConsentService pisConsentService;

    @Override
    public Optional<PaymentInitialisationResponse> createPeriodicPayment(PeriodicPayment periodicPayment) {
        String pisConsentId = pisConsentService.createPisConsentForPeriodicPaymentAndGetId(periodicPayment);

        if (StringUtils.isNotBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.ACCP);
            response.setPisConsentId(pisConsentId);
            response.setIban(periodicPayment.getDebtorAccount().getIban());

            return Optional.of(response);
        }

        return Optional.empty();
    }

    @Override
    public List<PaymentInitialisationResponse> createBulkPayment(List<SinglePayments> payments) {
        String pisConsentId = pisConsentService.createPisConsentForBulkPaymentAndGetId(payments);

        if (!StringUtils.isBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.ACCP);
            response.setPisConsentId(pisConsentId);
            response.setIban(payments.get(0).getDebtorAccount().getIban()); // TODO Establish order of payment creation with pis consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/159

            return new ArrayList<>(Collections.singletonList(response));
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<PaymentInitialisationResponse> createSinglePayment(SinglePayments singlePayment) {
        String pisConsentId = pisConsentService.createPisConsentForSinglePaymentAndGetId(singlePayment);

        if (!StringUtils.isBlank(pisConsentId)) {
            PaymentInitialisationResponse response = new PaymentInitialisationResponse();
            response.setTransactionStatus(TransactionStatus.RCVD);
            response.setPisConsentId(pisConsentId);
            response.setIban(singlePayment.getDebtorAccount().getIban());

            return Optional.of(response);
        }
        return Optional.empty();
    }
}
