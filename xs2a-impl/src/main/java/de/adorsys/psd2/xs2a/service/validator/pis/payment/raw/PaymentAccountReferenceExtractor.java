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

package de.adorsys.psd2.xs2a.service.validator.pis.payment.raw;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.AccountReferenceCollector;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
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
    private final RequestProviderService requestProviderService;

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
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Couldn't parse payment with paymentType {} from payment body {}",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentType, paymentBody);
            return Optional.empty();
        }
    }
}
