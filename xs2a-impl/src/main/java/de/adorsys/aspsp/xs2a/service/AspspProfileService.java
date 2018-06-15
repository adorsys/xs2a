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
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PaymentType;
import de.adorsys.aspsp.xs2a.spi.service.AspspProfileSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j
@Service
@RequiredArgsConstructor
public class AspspProfileService {
    private final AspspProfileSpi aspspProfileSpi;

    public List<PaymentProduct> getAvailablePaymentProducts() {
        return Optional.ofNullable(aspspProfileSpi.getAvailablePaymentProducts())
                   .map(list -> list.stream()
                                    .map(PaymentProduct::getByCode)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    public List<PaymentType> getAvailablePaymentTypes() {
        return Optional.ofNullable(aspspProfileSpi.getAvailablePaymentTypes())
                   .map(list -> list.stream()
                                    .map(PaymentType::getByValue)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    public Integer getFrequencyPerDay() {
        return Optional.ofNullable(aspspProfileSpi.getFrequencyPerDay())
                   .orElse(0);
    }
    
    public Boolean getTppSignatureRequired() {
        return Optional.ofNullable(aspspProfileSpi.getTppSignatureRequired())
                   .orElse(true);
    }
}
