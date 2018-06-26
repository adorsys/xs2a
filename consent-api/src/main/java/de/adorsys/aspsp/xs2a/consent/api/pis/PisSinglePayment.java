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

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class PisSinglePayment {
    private String paymentId;
    private String endToEndIdentification;
    private PisAccountReference debtorAccount;
    private String ultimateDebtor;
    private PisAmount instructedAmount;
    private PisAccountReference creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private PisAddress creditorAddress;
    private String ultimateCreditor;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private PisRemittance remittanceInformationStructured;
    private LocalDate requestedExecutionDate;
    private LocalDateTime requestedExecutionTime;
}
