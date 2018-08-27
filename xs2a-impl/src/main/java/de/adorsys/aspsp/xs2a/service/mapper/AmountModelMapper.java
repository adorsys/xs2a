/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.psd2.model.Amount;
import org.apache.commons.lang3.StringUtils;
import java.util.Currency;
import java.util.Optional;

public class AmountModelMapper {

    public static de.adorsys.aspsp.xs2a.domain.Amount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       de.adorsys.aspsp.xs2a.domain.Amount amountTarget = new de.adorsys.aspsp.xs2a.domain.Amount();
                       amountTarget.setContent(a.getAmount());
                       amountTarget.setCurrency(getCurrencyByCode(a.getCurrency()));
                       return amountTarget;
                   })
                   .orElse(null);
    }

    public static Amount mapToAmount(de.adorsys.aspsp.xs2a.domain.Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Amount amountTarget = new Amount();
                       amountTarget.setAmount(a.getContent());
                       amountTarget.setCurrency(a.getCurrency().getCurrencyCode());
                       return amountTarget;
                   })
                   .orElse(null);
    }

    private static Currency getCurrencyByCode(String code) {
        return StringUtils.isNotBlank(code)
                   ? Currency.getInstance(code)
                   : null;
    }
}
