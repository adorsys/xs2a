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

package de.adorsys.psd2.xs2a.service.validator.pis.payment.raw;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import de.adorsys.psd2.xs2a.service.mapper.PaymentModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAccountReferenceExtractor {
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final PaymentModelMapper paymentModelMapper;

    /**
     * Extracts account references that are present in JSON payment body
     *
     * @param paymentBody byte representation of JSON payment
     * @param paymentType payment type
     * @return account references
     */
    public Set<AccountReference> extractAccountReferences(byte[] paymentBody, PaymentType paymentType) {
        Optional<AccountReferenceCollector> accountReferenceCollector = mapToAccountReferenceCollector(paymentBody, paymentType);
        return accountReferenceCollector
                   .map(AccountReferenceCollector::getAccountReferences)
                   .orElse(Collections.emptySet());
    }

    private Optional<AccountReferenceCollector> mapToAccountReferenceCollector(byte[] paymentBody, PaymentType paymentType) {
        try {
            switch (paymentType) {
                case SINGLE:
                    PaymentInitiationJson paymentInitiationJson = xs2aObjectMapper.readValue(paymentBody, PaymentInitiationJson.class);
                    return Optional.ofNullable(paymentModelMapper.mapToXs2aPayment(paymentInitiationJson));
                case PERIODIC:
                    PeriodicPaymentInitiationJson periodicPaymentInitiationJson = xs2aObjectMapper.readValue(paymentBody, PeriodicPaymentInitiationJson.class);
                    return Optional.ofNullable(paymentModelMapper.mapToXs2aPayment(periodicPaymentInitiationJson));
                case BULK:
                    BulkPaymentInitiationJson bulkPaymentInitiationJson = xs2aObjectMapper.readValue(paymentBody, BulkPaymentInitiationJson.class);
                    return Optional.ofNullable(paymentModelMapper.mapToXs2aPayment(bulkPaymentInitiationJson));
                default:
                    throw new IllegalArgumentException("Unknown payment type: " + paymentType);
            }
        } catch (IOException ioe) {
            log.info("Couldn't parse payment with paymentType {} from payment body {}", paymentType, paymentBody);
            return Optional.empty();
        }
    }
}
