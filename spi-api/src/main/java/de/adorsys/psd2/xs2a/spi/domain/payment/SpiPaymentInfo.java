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

package de.adorsys.psd2.xs2a.spi.domain.payment;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * This class represents a common entity for all types of payments, preserving all payment-specific data as an array of bytes
 */
@Data
public class SpiPaymentInfo implements SpiPayment {
    private String paymentId;
    private String paymentProduct;
    private PaymentType paymentType;
    private TransactionStatus status;
    private byte[] paymentData;
    private List<SpiPsuData> psuDataList;
    private OffsetDateTime statusChangeTimestamp;

    public SpiPaymentInfo(String paymentProduct) {
        this.paymentProduct = paymentProduct;
    }

    @Override
    public TransactionStatus getPaymentStatus() {
        return status;
    }

    @Override
    public void setPaymentStatus(TransactionStatus status) {
        this.status = status;
    }
}
