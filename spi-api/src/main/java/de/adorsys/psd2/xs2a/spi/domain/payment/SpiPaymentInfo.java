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

package de.adorsys.psd2.xs2a.spi.domain.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private OffsetDateTime creationTimestamp;
    private String contentType;
    private String instanceId;

    @JsonCreator
    public SpiPaymentInfo(@JsonProperty("paymentProduct") String paymentProduct) {
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

    @Override
    public String getDebtorName() {
        return null;
    }

    @Override
    public void setDebtorName(String debtorName) {
        // Payment info does not have debtorName
    }
}
