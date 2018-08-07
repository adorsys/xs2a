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

package de.adorsys.aspsp.xs2a.consent.api.pis;

import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;


@Data
public class PisSinglePayment {
    private String paymentId;
    private String endToEndIdentification;
    private PisAccountReference debtorAccount;
    private String ultimateDebtor;
    private Currency currency;
    private BigDecimal amount;
    private PisAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private PisAddress creditorAddress;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private LocalDate requestedExecutionDate;
    private LocalDateTime requestedExecutionTime;
    private String ultimateCreditor;
    private String purposeCode;

    @Value
    public class PisAccountReference {
        private String iban;
        private String bban;
        private String pan;
        private String maskedPan;
        private String msisdn;
        private Currency currency;
    }

    @Value
    public class PisAddress {
        private String street;
        private String buildingNumber;
        private String city;
        private String postalCode;
        private String country;
    }
}
