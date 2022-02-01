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

package de.adorsys.psd2.xs2a.validator.payment;

import de.adorsys.psd2.validator.payment.CountryValidatorHolder;
import de.adorsys.psd2.validator.payment.PaymentBodyFieldsValidator;
import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.raw.DefaultPaymentBusinessValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.AustriaPaymentBodyFieldsValidatorImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AustriaPaymentValidatorHolder implements CountryValidatorHolder {

    private final AustriaPaymentBodyFieldsValidatorImpl austriaPaymentBodyFieldsValidator;
    private final DefaultPaymentBusinessValidatorImpl defaultPaymentBusinessValidator;

    @Override
    public String getCountryIdentifier() {
        return "AT";
    }

    @Override
    public PaymentBodyFieldsValidator getPaymentBodyFieldsValidator() {
        return austriaPaymentBodyFieldsValidator;
    }

    @Override
    public PaymentBusinessValidator getPaymentBusinessValidator() {
        return defaultPaymentBusinessValidator;
    }

    @Override
    public boolean isCustom() {
        return false;
    }
}
