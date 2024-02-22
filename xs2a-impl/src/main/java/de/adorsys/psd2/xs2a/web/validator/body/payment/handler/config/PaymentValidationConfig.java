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

    ValidationObject getReferenceMax140();
    ValidationObject getReferenceTypeMax140();
    ValidationObject getReferenceIssuerMax140();

    ValidationObject getExecutionRule();
    ValidationObject getCreditorId();
    ValidationObject getDayOfExecution();
    ValidationObject getMonthsOfExecution();

    ValidationObject getRemittanceInformationUnstructured();
    ValidationObject getChargeBearer();
}
