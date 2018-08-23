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

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.psd2.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Currency;
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

        target.setBalanceAmount(mapToAmount12(balance.getBalanceAmount()));
        if (balance.getBalanceType() != null) {
            target.setBalanceType(BalanceType.fromValue(balance.getBalanceType().toString()));
        }

        return target;
    }

    public static de.adorsys.psd2.model.Amount mapToAmount12(Amount amount) {
        de.adorsys.psd2.model.Amount amountTarget = new de.adorsys.psd2.model.Amount().amount(amount.getContent());
        amountTarget.setCurrency(amount.getCurrency().getCurrencyCode());
        return amountTarget;
    }

    public static de.adorsys.psd2.model.AccountReport mapToAccountReport(AccountReport accountReport) {
        de.adorsys.psd2.model.AccountReport target = new de.adorsys.psd2.model.AccountReport();
        BeanUtils.copyProperties(accountReport, target);

        return target;
    }

    public static <T> AccountReference mapToXs2aAccountReference(T reference) {
        AccountReference xs2aReference = new AccountReference();
        BeanUtils.copyProperties(reference, xs2aReference);
        return xs2aReference;
    }

    public static <T> T mapToAccountReference12(AccountReference reference) {
        T accountReference = null;

        if (StringUtils.isNotBlank(reference.getIban())) {
            accountReference = (T) new AccountReferenceIban().iban(reference.getIban());
            ((AccountReferenceIban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getBban())) {
            accountReference = (T) new AccountReferenceBban().bban(reference.getBban());
            ((AccountReferenceBban) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getPan())) {
            accountReference = (T) new AccountReferencePan().pan(reference.getPan());
            ((AccountReferencePan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMaskedPan())) {
            accountReference = (T) new AccountReferenceMaskedPan().maskedPan(reference.getMaskedPan());
            ((AccountReferenceMaskedPan) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        } else if (StringUtils.isNotBlank(reference.getMsisdn())) {
            accountReference = (T) new AccountReferenceMsisdn().msisdn(reference.getMsisdn());
            ((AccountReferenceMsisdn) accountReference).setCurrency(reference.getCurrency().getCurrencyCode());
        }
        return accountReference;
    }

    public static Address mapToAddress12(de.adorsys.aspsp.xs2a.domain.address.Address address) {
        Address targetAddress = new Address().street(address.getStreet());
        targetAddress.setStreet(address.getStreet());
        targetAddress.setBuildingNumber(address.getBuildingNumber());
        targetAddress.setCity(address.getCity());
        targetAddress.setPostalCode(address.getPostalCode());
        targetAddress.setCountry(address.getCountry().getCode());
        return targetAddress;
    }

    public static de.adorsys.aspsp.xs2a.domain.address.Address mapToXs2aAddress(Address address) {
        de.adorsys.aspsp.xs2a.domain.address.Address targetAddress = new de.adorsys.aspsp.xs2a.domain.address.Address();
        targetAddress.setStreet(address.getStreet());
        targetAddress.setBuildingNumber(address.getBuildingNumber());
        targetAddress.setCity(address.getCity());
        targetAddress.setPostalCode(address.getPostalCode());
        de.adorsys.aspsp.xs2a.domain.address.CountryCode code = new de.adorsys.aspsp.xs2a.domain.address.CountryCode();
        code.setCode(address.getCountry());
        targetAddress.setCountry(code);
        return targetAddress;
    }

    public static Amount mapToXs2aAmount(de.adorsys.psd2.model.Amount amount) {
        Amount targetAmount = new Amount();
        targetAmount.setContent(amount.getAmount());
        targetAmount.setCurrency(Currency.getInstance(amount.getCurrency()));
        return targetAmount;
    }
}
