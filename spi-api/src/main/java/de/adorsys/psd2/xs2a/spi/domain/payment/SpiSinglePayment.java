/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.spi.domain.payment;

import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SpiSinglePayment implements SpiPayment {
    private String paymentId;
    private String endToEndIdentification;
    private String instructionIdentification;
    private SpiAccountReference debtorAccount;
    private SpiAmount instructedAmount;
    private SpiAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorId;
    private String creditorName;
    private SpiAddress creditorAddress;
    private SpiTransactionStatus paymentStatus;
    protected String paymentProduct;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;
    private List<SpiPsuData> psuDataList;
    private OffsetDateTime statusChangeTimestamp;
    private String ultimateDebtor;
    private String ultimateCreditor;
    private SpiPisPurposeCode purposeCode;
    private String remittanceInformationUnstructured;
    private List<String> remittanceInformationUnstructuredArray;
    private SpiRemittance remittanceInformationStructured;
    private List<SpiRemittance> remittanceInformationStructuredArray;
    private OffsetDateTime creationTimestamp;
    private String contentType;
    private String debtorName;
    private String instanceId;
    private String chargeBearer;

    public SpiSinglePayment(String paymentProduct) {
        this.paymentProduct = paymentProduct;
    }

    @Override
    public SpiPaymentType getPaymentType() {
        return SpiPaymentType.SINGLE;
    }

    @Override
    public String getPaymentProduct() {
        return paymentProduct;
    }

    @Override
    public SpiTransactionStatus getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public void setPaymentStatus(SpiTransactionStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
