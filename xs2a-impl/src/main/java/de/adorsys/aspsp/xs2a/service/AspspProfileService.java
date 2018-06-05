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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.spi.impl.AspspProfileSpiImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
@Service
@RequiredArgsConstructor
public class AspspProfileService {
    private final AspspProfileSpiImpl aspspProfileSpi;

    public List<PaymentProduct> getAvailablePaymentProducts() {
        List<String> paymentProductsStr = aspspProfileSpi.getAvailablePaymentProducts();
        if (CollectionUtils.isEmpty(paymentProductsStr)) {
            return Collections.emptyList();
        }

        return paymentProductsStr.stream()
                   .map(this::mapToPaymentProductFromString)
                   .collect(Collectors.toList());
    }

    private PaymentProduct mapToPaymentProductFromString(String paymentProductStr) {
        try {
            return PaymentProduct.forValue(paymentProductStr);
        } catch (IllegalArgumentException ex) {
            log.warn("Payment product is not correct: " + paymentProductStr);
            return null;
        }
    }
}
