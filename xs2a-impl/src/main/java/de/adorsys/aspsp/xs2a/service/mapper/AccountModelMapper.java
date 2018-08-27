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

import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.psd2.model.AccountList;
import de.adorsys.psd2.model.BalanceList;
import de.adorsys.psd2.model.BalanceType;
import de.adorsys.psd2.model.ReadBalanceResponse200;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Map;

public final class AccountModelMapper {

    public static AccountList mapToAccountList(Map<String, List<AccountDetails>> accountDetailsList) {
        AccountList accountList = new AccountList();
        accountDetailsList.forEach((s, accountDetails) -> {
            de.adorsys.psd2.model.AccountDetails detailsTarget = new de.adorsys.psd2.model.AccountDetails();
            BeanUtils.copyProperties(accountDetails, detailsTarget);
            accountList.addAccountsItem(detailsTarget);
        });

        return accountList;
    }

    public static de.adorsys.psd2.model.AccountDetails mapToAccountDetails(AccountDetails accountDetails) {
        de.adorsys.psd2.model.AccountDetails detailsTarget = new de.adorsys.psd2.model.AccountDetails();
        BeanUtils.copyProperties(accountDetails, detailsTarget);

        detailsTarget.setBalances(new BalanceList());

        accountDetails.getBalances().forEach(balance -> {
            detailsTarget.getBalances().add(mapToBalance(balance));
        });

        return detailsTarget;
    }

    public static ReadBalanceResponse200 mapToBalance(List<Balance> balances) {
        ReadBalanceResponse200 response = new ReadBalanceResponse200();
        BalanceList balancesResponse = new BalanceList();
        response.setBalances(balancesResponse);

        balances.forEach(balance -> {
            response.getBalances().add(mapToBalance(balance));
        });

        return response;
    }

    public static de.adorsys.psd2.model.Balance mapToBalance(Balance balance) {
        de.adorsys.psd2.model.Balance target = new de.adorsys.psd2.model.Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(AmountModelMapper.mapToAmount(balance.getBalanceAmount()));
        if (balance.getBalanceType() != null) {
            target.setBalanceType(BalanceType.fromValue(balance.getBalanceType().toString()));
        }

        return target;
    }

    public static de.adorsys.psd2.model.AccountReport mapToAccountReport(AccountReport accountReport) {
        de.adorsys.psd2.model.AccountReport target = new de.adorsys.psd2.model.AccountReport();
        BeanUtils.copyProperties(accountReport, target);

        return target;
    }
}
