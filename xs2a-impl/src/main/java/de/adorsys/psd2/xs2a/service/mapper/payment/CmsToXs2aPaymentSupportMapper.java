/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.payment;

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
