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
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CountryPaymentValidatorResolver {

    private static final String DEFAULT = "DE"; // Germany and default
    private final Map<String, CountryValidatorHolder> countryValidators = new HashMap<>();

    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private List<CountryValidatorHolder> countryValidatorHolders;

    @Autowired
    public CountryPaymentValidatorResolver(AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                           List<CountryValidatorHolder> countryValidatorHolders) {
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
        this.countryValidatorHolders = countryValidatorHolders;
        initContext();
    }

    public PaymentBodyFieldsValidator getPaymentBodyFieldValidator() {
        return getCountryValidatorHolder().getPaymentBodyFieldsValidator();
    }

    public PaymentBusinessValidator getPaymentBusinessValidator() {
        return getCountryValidatorHolder().getPaymentBusinessValidator();
    }

    CountryValidatorHolder getCountryValidatorHolder() {
        String supportedPaymentCountryCode = aspspProfileServiceWrapper.getSupportedPaymentCountryValidation();
        return countryValidators.getOrDefault(StringUtils.upperCase(supportedPaymentCountryCode),
                                              countryValidators.get(DEFAULT));
    }

    private void initContext() {
        countryValidatorHolders.forEach(m -> {
            if (m.isCustom() || !countryValidators.containsKey(m.getCountryIdentifier())) {
                countryValidators.put(m.getCountryIdentifier(), m);
            }
        });
    }
}
