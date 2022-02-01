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

package de.adorsys.psd2.validator.payment;

public interface CountryValidatorHolder {
    /**
     * Returns country identifier for which payment validators will be applied
     *
     * @return country identifier
     */
    String getCountryIdentifier();

    /**
     * Returns payment body fields validator for validating format of the payment
     *
     * @return body validator
     */
    PaymentBodyFieldsValidator getPaymentBodyFieldsValidator();

    /**
     * Returns payment business validator for executing custom validation according to specific business rules.
     * <p>
     * Will be executed after validating payment fields, but before any call to SPI.
     *
     * @return business validator
     */
    PaymentBusinessValidator getPaymentBusinessValidator();

    /**
     * Defines either country validation holder is custom or not.
     * Needs to override default configurations on xs2a connector.
     *
     * @return true for custom holders (default value), false - for predefined holders.
     */
    default boolean isCustom() {
        return true;
    }
}
