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
