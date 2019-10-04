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

import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CountryPaymentValidatorResolver {

    private static final Map<String, PaymentValidationConfig> countryValidatorConfigs = new HashMap<>();

    private static final String DE = "DE"; // Germany and default
    private static final String AT = "AT"; // Austria
    private static final String CH = "CH"; // Switzerland

    static {
        countryValidatorConfigs.put(DE, new DefaultPaymentValidationConfigImpl());
        countryValidatorConfigs.put(AT, new AustriaValidationConfigImpl());
        countryValidatorConfigs.put(CH, new SwitzerlandValidationConfigImpl());
    }

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    public PaymentValidationConfig getValidationConfig() {
        String supportedPaymentCountryCode = aspspProfileServiceWrapper.getSupportedPaymentCountryValidation();
        return countryValidatorConfigs.getOrDefault(StringUtils.upperCase(supportedPaymentCountryCode),
                                                    countryValidatorConfigs.get(DE));
    }
}
