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

package de.adorsys.aspsp.xs2a.domain.account;


import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import org.junit.Assert;
import org.junit.Test;

import java.util.Currency;

import static de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper.mapToBalance;

public class AccountModelMapperTest {

    @Test
    public void testBalanceMapping() {
        Balance balance = new Balance();

        Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("EUR"));
        amount.setContent("1000");

        balance.setBalanceAmount(amount);

        de.adorsys.psd2.model.Balance result = mapToBalance(balance);

        Assert.assertEquals(balance.getBalanceAmount().getContent(), result.getBalanceAmount().getAmount());
        Assert.assertEquals(balance.getBalanceAmount().getCurrency().getCurrencyCode(), result.getBalanceAmount().getCurrency());

    }
}
