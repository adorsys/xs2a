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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

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
