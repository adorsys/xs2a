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

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SpiCommonPayment implements SpiPayment {
    private String paymentId;
    private PaymentType paymentType;
    private String paymentProduct;
    private TransactionStatus paymentStatus;
    private OffsetDateTime statusChangeTimestamp;
    private List<SpiPsuData> psuDataList;
    private String contentType;

    private byte[] paymentData;
    private OffsetDateTime creationTimestamp;
    private String instanceId;

    @Override
    public String getDebtorName() {
        return null;
    }

    @Override
    public void setDebtorName(String debtorName) {
        // Common payment does not have debtorName
    }
}
