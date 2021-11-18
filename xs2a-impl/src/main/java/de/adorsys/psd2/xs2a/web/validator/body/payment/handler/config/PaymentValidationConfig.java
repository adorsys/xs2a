/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config;

import de.adorsys.psd2.validator.payment.config.ValidationObject;

public interface PaymentValidationConfig {

    ValidationObject getEndToEndIdentification();
    ValidationObject getInstructionIdentification();
    ValidationObject getUltimateDebtor();
    ValidationObject getUltimateCreditor();
    ValidationObject getCreditorName();
    ValidationObject getDebtorName();

    //address
    ValidationObject getStreetName();
    ValidationObject getBuildingNumber();
    ValidationObject getTownName();
    ValidationObject getPostCode();

    //account reference
    ValidationObject getPan();
    ValidationObject getMaskedPan();
    ValidationObject getMsisdn();

    //remittance
    ValidationObject getReference();
    ValidationObject getReferenceType();
    ValidationObject getReferenceIssuer();

    ValidationObject getExecutionRule();
    ValidationObject getCreditorId();
    ValidationObject getDayOfExecution();
    ValidationObject getMonthsOfExecution();

    ValidationObject getRemittanceInformationStructured();
    ValidationObject getChargeBearer();
}
