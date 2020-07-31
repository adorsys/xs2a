/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
