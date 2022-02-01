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

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
    AmountModelMapper.class, PurposeCodeMapper.class,
    Xs2aAddressMapper.class, AspspProfileServiceWrapper.class,
    ReportExchangeMapper.class, BalanceMapper.class,
    DayOfExecutionMapper.class})
public abstract class AccountModelMapper {
    private static final List<MulticurrencyAccountLevel> MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS = Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT);

    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Autowired
    protected BalanceMapper balanceMapper;

    @Mapping(target = "currency", source = "currency.currencyCode")
    @Mapping(target = "other", expression = "java(mapToOtherType(accountReference.getOther()))")
    public abstract de.adorsys.psd2.model.AccountReference mapToAccountReference(AccountReference accountReference);

    public abstract List<de.adorsys.psd2.model.AccountReference> mapToAccountReferences(List<AccountReference> accountReferences);

    public AccountList mapToAccountList(Xs2aAccountListHolder xs2aAccountListHolder) {
        List<Xs2aAccountDetails> accountDetailsList = xs2aAccountListHolder.getAccountDetails();

        List<AccountDetails> details = accountDetailsList.stream()
                                           .map(this::mapToAccountDetails)
                                           .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public InlineResponse200 mapToInlineResponse200(Xs2aAccountDetailsHolder xs2aAccountDetailsHolder) {
        InlineResponse200 inlineResponse200 = new InlineResponse200();
        inlineResponse200.setAccount(mapToAccountDetails(xs2aAccountDetailsHolder.getAccountDetails()));
        return inlineResponse200;
    }

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountDetails.getLinks()))")
    @Mapping(target = "status", source = "accountStatus")
    @Mapping(target = "usage", source = "usageType")
    @Mapping(target = "currency", expression = "java(mapToAccountDetailsCurrency(accountDetails.getCurrency()))")
    public abstract AccountDetails mapToAccountDetails(Xs2aAccountDetails accountDetails);

    @Mapping(target = "account", source = "xs2aAccountReference")
    public abstract ReadAccountBalanceResponse200 mapToBalance(Xs2aBalancesReport balancesReport);

    protected String mapToAccountDetailsCurrency(Currency currency) {
        return Optional.ofNullable(currency)
                   .map(Currency::getCurrencyCode)
                   .orElseGet(this::getMulticurrencyRepresentationOrNull);
    }

    protected OtherType mapToOtherType(String other){
        return other == null
                   ? null
                   : new OtherType().identification(other);
    }

    private String getMulticurrencyRepresentationOrNull() {
        return MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS.contains(aspspProfileServiceWrapper.getMulticurrencyAccountLevel()) ? "XXX" : null;
    }
}

