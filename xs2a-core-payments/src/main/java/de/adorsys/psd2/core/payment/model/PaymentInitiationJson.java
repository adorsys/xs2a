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

package de.adorsys.psd2.core.payment.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PaymentInitiationJson {
    private String endToEndIdentification;
    private String instructionIdentification;
    private String debtorName;
    private AccountReference debtorAccount;
    private String ultimateDebtor;
    private Xs2aAmount instructedAmount;
    private AccountReference creditorAccount;
    private String creditorAgent;
    private String creditorAgentName;
    private String creditorName;
    private Address creditorAddress;
    private String creditorId;
    private String ultimateCreditor;
    private PurposeCode purposeCode;
    private ChargeBearer chargeBearer;
    private String remittanceInformationUnstructured;
    private List<String> remittanceInformationUnstructuredArray;
    private RemittanceInformationStructured remittanceInformationStructured;
    private List<RemittanceInformationStructured> remittanceInformationStructuredArray;
    private LocalDate requestedExecutionDate;
}

