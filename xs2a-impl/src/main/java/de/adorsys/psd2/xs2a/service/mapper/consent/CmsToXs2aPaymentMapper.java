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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CmsToXs2aPaymentMapper {

    public CommonPayment mapToXs2aCommonPayment(CommonPaymentData response) {
        return Optional.ofNullable(response)
                   .map(r -> {
                            CommonPayment commonPayment = new CommonPayment();
                            commonPayment.setPaymentId(r.getExternalId());
                            commonPayment.setTransactionStatus(r.getTransactionStatus());
                            commonPayment.setPaymentProduct(r.getPaymentProduct());
                            commonPayment.setPaymentType(r.getPaymentType());
                            commonPayment.setPaymentData(r.getPaymentData());
                            commonPayment.setPsuDataList(r.getPsuData());
                            commonPayment.setStatusChangeTimestamp(r.getStatusChangeTimestamp());
                            commonPayment.setCreationTimestamp(r.getCreationTimestamp());
                            return commonPayment;
                        }
                   )
                   .orElse(null);
    }
}
