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

package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.psd2.consent.api.pis.PisPaymentType;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiRemittance;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @deprecated since 1.8. Will be removed in 1.10
 * @see de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment
 */
@Data
@Deprecated
public class AspspPayment {
    @Id
    private String paymentId;
    private String endToEndIdentification;
    private SpiAccountReference debtorAccount;
    @Deprecated // Since 1.2
    private String ultimateDebtor;
    private SpiAmount instructedAmount;
    private SpiAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private SpiAddress creditorAddress;
    @Deprecated // Since 1.2
    private String ultimateCreditor;
    @Deprecated // Since 1.2
    private String purposeCode;
    private String remittanceInformationUnstructured;
    @Deprecated // Since 1.2
    private SpiRemittance remittanceInformationStructured;
    @Deprecated // Since 1.2
    private LocalDate requestedExecutionDate;
    @Deprecated // Since 1.2
    private LocalDateTime requestedExecutionTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private String frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022
    private int dayOfExecution; //Day here max 31
    private PisPaymentType pisPaymentType;
    private SpiTransactionStatus paymentStatus;
    private String bulkId;

    public AspspPayment() {}

    public AspspPayment(PisPaymentType pisPaymentType) {
        this.pisPaymentType = pisPaymentType;
    }
}
