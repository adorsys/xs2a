/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

    private String getMulticurrencyRepresentationOrNull() {
        return MULTICURRENCY_ACCOUNT_AGGREGATION_LEVELS.contains(aspspProfileServiceWrapper.getMulticurrencyAccountLevel()) ? "XXX" : null;
    }

    protected OtherType mapToOtherType(String other){
        return other == null ? null : new OtherType().identification(other);
    }
}

