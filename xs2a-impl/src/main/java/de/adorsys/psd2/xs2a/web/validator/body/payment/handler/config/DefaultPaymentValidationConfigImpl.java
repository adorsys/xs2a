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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config;

import de.adorsys.psd2.validator.payment.config.Occurrence;
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import lombok.Data;

@Data
public class DefaultPaymentValidationConfigImpl implements PaymentValidationConfig {
    protected ValidationObject endToEndIdentification = new ValidationObject(35);
    protected ValidationObject instructionIdentification = new ValidationObject(35);
    protected ValidationObject ultimateDebtor = new ValidationObject(70);
    protected ValidationObject ultimateCreditor = new ValidationObject(70);
    protected ValidationObject creditorName = new ValidationObject(Occurrence.REQUIRED, 70);
    protected ValidationObject debtorName = new ValidationObject(Occurrence.OPTIONAL, 70);
    protected ValidationObject chargeBearer = new ValidationObject(Occurrence.OPTIONAL, 4);

    //address
    protected ValidationObject streetName = new ValidationObject(100);
    protected ValidationObject buildingNumber = new ValidationObject(20);
    protected ValidationObject townName = new ValidationObject(100);
    protected ValidationObject postCode = new ValidationObject(35);

    //account reference
    protected ValidationObject pan = new ValidationObject(35);
    protected ValidationObject maskedPan = new ValidationObject(35);
    protected ValidationObject msisdn = new ValidationObject(35);

    //remittance
    protected ValidationObject reference = new ValidationObject(Occurrence.REQUIRED, 35);
    protected ValidationObject referenceType = new ValidationObject(35);
    protected ValidationObject referenceIssuer = new ValidationObject(35);

    protected ValidationObject executionRule = new ValidationObject(Occurrence.REQUIRED, 140);
    protected ValidationObject creditorId = new ValidationObject(Occurrence.NONE, 0);
    protected ValidationObject dayOfExecution = new ValidationObject(2);
    protected ValidationObject monthsOfExecution = new ValidationObject(11);

    protected ValidationObject remittanceInformationStructured = new ValidationObject(140);
}
