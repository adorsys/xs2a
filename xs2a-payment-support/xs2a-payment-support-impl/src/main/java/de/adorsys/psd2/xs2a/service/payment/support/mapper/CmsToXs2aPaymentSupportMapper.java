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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CmsToXs2aPaymentSupportMapper {
    private final RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    public SinglePayment mapToSinglePayment(CommonPaymentData commonPayment) {
        if (commonPayment == null) {
            return null;
        }

        byte[] paymentData = commonPayment.getPaymentData();
        SinglePayment originalSinglePayment = rawToXs2aPaymentMapper.mapToSinglePayment(paymentData);
        return enrichWithCommonProperties(originalSinglePayment, commonPayment);
    }

    public PeriodicPayment mapToPeriodicPayment(CommonPaymentData commonPayment) {
        if (commonPayment == null) {
            return null;
        }

        byte[] paymentData = commonPayment.getPaymentData();
        PeriodicPayment originalPeriodicPayment = rawToXs2aPaymentMapper.mapToPeriodicPayment(paymentData);
        return enrichWithCommonProperties(originalPeriodicPayment, commonPayment);
    }

    public BulkPayment mapToBulkPayment(CommonPaymentData commonPayment) {
        if (commonPayment == null) {
            return null;
        }

        byte[] paymentData = commonPayment.getPaymentData();
        BulkPayment originalBulkPayment = rawToXs2aPaymentMapper.mapToBulkPayment(paymentData);
        BulkPayment bulkPayment = enrichWithCommonProperties(originalBulkPayment, commonPayment);

        List<SinglePayment> payments = bulkPayment.getPayments();
        if (payments != null) {
            bulkPayment.setPayments(enrichBulkPaymentParts(commonPayment, payments));
        }

        return bulkPayment;
    }

    private <T extends CommonPayment> T enrichWithCommonProperties(T xs2aCommonPayment, CommonPaymentData commonPaymentData) {
        xs2aCommonPayment.setPaymentId(commonPaymentData.getExternalId());
        xs2aCommonPayment.setPaymentProduct(commonPaymentData.getPaymentProduct());
        xs2aCommonPayment.setTransactionStatus(commonPaymentData.getTransactionStatus());
        xs2aCommonPayment.setStatusChangeTimestamp(commonPaymentData.getStatusChangeTimestamp());
        xs2aCommonPayment.setCreationTimestamp(commonPaymentData.getCreationTimestamp());
        xs2aCommonPayment.setContentType(commonPaymentData.getContentType());
        xs2aCommonPayment.setInstanceId(commonPaymentData.getInstanceId());
        return xs2aCommonPayment;
    }

    private List<SinglePayment> enrichBulkPaymentParts(CommonPaymentData commonPaymentData, List<SinglePayment> bulkPaymentParts) {
        List<SinglePayment> enrichedPayments = new ArrayList<>(bulkPaymentParts);

        enrichedPayments.forEach(pmt -> {
            pmt.setPaymentId(commonPaymentData.getExternalId());
            pmt.setTransactionStatus(commonPaymentData.getTransactionStatus());
            pmt.setPsuDataList(commonPaymentData.getPsuData());
            pmt.setStatusChangeTimestamp(commonPaymentData.getStatusChangeTimestamp());
        });

        return enrichedPayments;
    }
}
