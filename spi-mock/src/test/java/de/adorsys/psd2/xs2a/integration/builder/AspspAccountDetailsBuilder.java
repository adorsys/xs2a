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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspBalanceType;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

public class AspspAccountDetailsBuilder {
    private static final BigDecimal BALANCE_AMOUNT = new BigDecimal("1000");

    public static AspspAccountDetails buildAccountDetails(String iban, Currency currency) {
        return new AspspAccountDetails(null, null, iban, null, null, null, null, currency, "account name", null, null, null, null, null, null, null, null, getBalances(currency));
    }

    private static List<AspspAccountBalance> getBalances(Currency currency) {
        AspspAccountBalance balance = new AspspAccountBalance();
        balance.setSpiBalanceAmount(new AspspAmount(currency, BALANCE_AMOUNT));
        balance.setSpiBalanceType(AspspBalanceType.INTERIM_AVAILABLE);
        return Collections.singletonList(balance);
    }

}
