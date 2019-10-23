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

package de.adorsys.psd2.xs2a.service.spi.payment;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpiPaymentServiceResolver {
    private final CommonPaymentSpi commonPaymentSpi;
    private final SinglePaymentSpi singlePaymentSpi;
    private final PeriodicPaymentSpi periodicPaymentSpi;
    private final BulkPaymentSpi bulkPaymentSpi;

    public PaymentSpi getPaymentService(GetPisAuthorisationResponse pisAuthorisationResponse,
                                           PaymentType paymentType) {
        if (CollectionUtils.isEmpty(pisAuthorisationResponse.getPayments())) {
            return commonPaymentSpi;
        }

        if (PaymentType.SINGLE == paymentType) {
            return singlePaymentSpi;
        } else if (PaymentType.PERIODIC == paymentType) {
            return periodicPaymentSpi;
        } else {
            return bulkPaymentSpi;
        }
    }

}
