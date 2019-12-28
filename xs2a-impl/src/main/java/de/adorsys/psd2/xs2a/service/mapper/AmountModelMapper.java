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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AmountModelMapper {
    private final ValueValidatorService valueValidatorService;

    public Xs2aAmount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Xs2aAmount amountTarget = new Xs2aAmount();
                       amountTarget.setAmount(a.getAmount());
                       amountTarget.setCurrency(getCurrencyByCode(a.getCurrency()));
                       valueValidatorService.validate(amountTarget);
                       return amountTarget;
                   })
                   .orElse(null);
    }

    public Amount mapToAmount(Xs2aAmount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Amount amountTarget = new Amount();
                       amountTarget.setAmount(a.getAmount());
                       if (a.getCurrency() != null) {
                           amountTarget.setCurrency(a.getCurrency().getCurrencyCode());
                       }
                       return amountTarget;
                   })
                   .orElse(null);
    }

    private Currency getCurrencyByCode(String code) {
        return StringUtils.isNotBlank(code)
                   ? Currency.getInstance(code)
                   : null;
    }
}
