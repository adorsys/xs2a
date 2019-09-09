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

package de.adorsys.psd2.xs2a.web.validator.body.payment.config;

import lombok.Data;

@Data
public class DefaultPaymentValidationConfigImpl implements PaymentValidationConfig {
    private ValidationObject endToEndIdentification;
    private ValidationObject ultimateDebtor;
    private ValidationObject ultimateCreditor;
    private ValidationObject creditorName;

    //address
    private ValidationObject streetName;
    private ValidationObject buildingNumber;
    private ValidationObject townName;
    private ValidationObject postCode;

    //account reference
    private ValidationObject pan;
    private ValidationObject maskedPan;
    private ValidationObject msisdn;

    //remittance
    private ValidationObject reference;
    private ValidationObject referenceType;
    private ValidationObject referenceIssuer;

    private ValidationObject executionRule;
    private ValidationObject creditorId;
}
