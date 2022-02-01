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

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsSinglePayment extends BaseCmsPayment {
    private String endToEndIdentification;
    private String instructionIdentification;
    private AccountReference debtorAccount;
    private String debtorName;
    private CmsAmount instructedAmount;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private CmsAddress creditorAddress;
    private String remittanceInformationUnstructured;
    private TransactionStatus paymentStatus;
    private LocalDate requestedExecutionDate;
    private OffsetDateTime requestedExecutionTime;
    private String ultimateDebtor;
    private String ultimateCreditor;
    private String purposeCode;
    private CmsRemittance remittanceInformationStructured;
    private List<CmsRemittance> remittanceInformationStructuredArray;
    private String chargeBearer;

    public CmsSinglePayment(String paymentProduct) {
        setPaymentProduct(paymentProduct);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }

}
