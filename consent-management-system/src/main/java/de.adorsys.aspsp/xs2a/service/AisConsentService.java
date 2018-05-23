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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.account.AccountInfoDetail;
import de.adorsys.aspsp.xs2a.domain.AisAccount;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.domain.AisConsentStatus;
import de.adorsys.aspsp.xs2a.domain.TypeAccess;
import de.adorsys.aspsp.xs2a.exception.ConsentException;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class AisConsentService {
    private final AccountSpi accountSpi;
    private final AisConsentRepository aisConsentRepository;

    public Optional<String> createConsent(AisConsentRequest request){
        AisConsent consent = new AisConsent();
        consent.setExternalId(UUID.randomUUID().toString());
        consent.setConsentStatus(AisConsentStatus.RECEIVED);
        consent.setFrequencyPerDay(request.getFrequencyPerDay());
        consent.setUsageCounter(request.getFrequencyPerDay());
        consent.setRequestDate(LocalDateTime.now());
        consent.setExpireDate(request.getValidUntil());
        consent.setPsuId(request.getPsuId());
        consent.setTppId(request.getTppId());
        consent.addAccounts(readAccounts(request));
        aisConsentRepository.save(consent);
        return Optional.of(consent.getExternalId());
    }

    private List<AisAccount> readAccounts(AisConsentRequest request) {
        return request.getAccess().isAllAccountAccess()
            ? readAccountsByPsuId(request.getAccess(), request.getPsuId())
            : readAccountsByIban(request.getAccess());
    }

    private List<AisAccount> readAccountsByPsuId(AisAccountAccessInfo access, String psuId){
        if(access.isAllAccountAccess() && StringUtils.isBlank(psuId)){
            throw new ConsentException("Psu id must not be empty");
        }
        AccountInfoDetail info = buildAccountInfoDetailByPsu(access, psuId);
        return buildAccounts(info.getIbansAccess());
    }

    private AccountInfoDetail buildAccountInfoDetailByPsu(AisAccountAccessInfo access, String psuId) {
        AccountInfoDetail info = new AccountInfoDetail();
        List<SpiAccountDetails> accountDetails = accountSpi.readAccountsByPsuId(psuId);
        accountDetails.forEach(a -> info.addAccountAccess(a.getIban(), a.getCurrency(), buildAccess(access)));
        return info;
    }

    private Set<TypeAccess> buildAccess(AisAccountAccessInfo access) {
        return access.isAllPsd2()
            ? EnumSet.allOf(TypeAccess.class)
            : EnumSet.of(TypeAccess.ACCOUNT);
    }

    private List<AisAccount> readAccountsByIban(AisAccountAccessInfo access) {
        Map<String, AccountInfoDetail.InfoDetail> accountsDetailByAccess = buildAccountsDetailByAccess(access);
        Map<String, Set<Currency>> bankAccounts = getAccountAndGroupByIban(accountsDetailByAccess);

        accountsDetailByAccess = filterAccessAccounts(accountsDetailByAccess, bankAccounts);
        return buildAccounts(accountsDetailByAccess);
    }

    private Map<String, AccountInfoDetail.InfoDetail> filterAccessAccounts(Map<String, AccountInfoDetail.InfoDetail> accountsDetailByAccess, Map<String, Set<Currency>> bankAccounts) {
        accountsDetailByAccess.entrySet().stream()
            .filter(e -> bankAccounts.containsKey(e.getKey()))
            .forEach(e ->  e.getValue().refreshCurrency(bankAccounts.get(e.getKey())));
        return accountsDetailByAccess;
    }

    private Map<String, Set<Currency>> getAccountAndGroupByIban(Map<String, AccountInfoDetail.InfoDetail> accountsDetailByAccess) {
        List<SpiAccountDetails> accountDetails = Optional.ofNullable(accountSpi.readAccountDetailsByIbans(accountsDetailByAccess.keySet()))
            .orElse(Collections.emptyList());

        return accountDetails.stream()
            .collect(Collectors.groupingBy(SpiAccountDetails::getIban, Collectors.mapping(SpiAccountDetails::getCurrency, toSet())));
    }


    private Map<String, AccountInfoDetail.InfoDetail> buildAccountsDetailByAccess(AisAccountAccessInfo access) {
        List<AisAccountInfo> accounts = access.getAccounts();
        List<AisAccountInfo> balances = access.getBalances();
        List<AisAccountInfo> transactions = access.getTransactions();

        AccountInfoDetail info = new AccountInfoDetail();
        fillAccountInfoDetail(accounts, info, TypeAccess.ACCOUNT);
        fillAccountInfoDetail(balances, info, TypeAccess.BALANCE);
        fillAccountInfoDetail(transactions, info, TypeAccess.TRANSACTION);
        return info.getIbansAccess();
    }

    private void fillAccountInfoDetail(List<AisAccountInfo> references, AccountInfoDetail info, TypeAccess typeAccess) {
        references.forEach(a -> info.addAccountAccess(a.getIban(), Currency.getInstance(a.getCurrency()), typeAccess));
    }

    private List<AisAccount> buildAccounts(Map<String, AccountInfoDetail.InfoDetail> accountsDetail) {
        return accountsDetail
            .entrySet()
            .stream()
            .map(e -> buildAccount(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }

    private AisAccount buildAccount(String iban, AccountInfoDetail.InfoDetail accountsDetail) {
        AisAccount account = new AisAccount(iban);
        account.addAccesses(accountsDetail.getAccesses());
        account.addCurrencies(accountsDetail.getCurrencies());
        return account;
    }
}
