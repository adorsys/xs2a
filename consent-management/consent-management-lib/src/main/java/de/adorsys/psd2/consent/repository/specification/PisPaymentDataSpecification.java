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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.consent.repository.specification.EntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute;

@Service
public class PisPaymentDataSpecification {
    public Specification<PisPaymentData> byPaymentIdAndInstanceId(String paymentId, String instanceId) {
        return Optional.of(Specification.<PisPaymentData>where(provideSpecificationForEntityAttribute(EntityAttribute.PAYMENT_ID_ATTRIBUTE, paymentId)))
                   .map(s -> s.and(provideSpecificationForEntityAttribute(EntityAttribute.INSTANCE_ID_ATTRIBUTE, instanceId)))
                   .orElse(null);
    }
}
