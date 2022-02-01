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
