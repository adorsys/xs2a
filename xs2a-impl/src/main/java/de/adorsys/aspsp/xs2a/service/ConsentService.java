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

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AvailableAccessRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final ConsentSpi consentSpi;
    private final ConsentMapper consentMapper;
    private final AisConsentService aisConsentService;
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;


    /*
       Validation


       // TODO: refactor according to task https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/131
    public Map<String, Set<AccessAccountInfo>> checkAvailableAccessAccount(AvailableAccessRequest request) {
        Optional<AisConsent> consent = aisConsentRepository.findByExternalId(request.getConsentId());
        AisConsent aisConsent = consent.orElseThrow(() -> new ConsentException("Consent id not found"));
        if (!EnumSet.of(VALID, RECEIVED).contains(aisConsent.getConsentStatus())) {
            throw new ConsentException("Consent status: " + aisConsent.getConsentStatus());
        }
        if(aisConsent.isExpired()){
            aisConsent.setConsentStatus(EXPIRED);
            aisConsentRepository.save(aisConsent);
            throw new ConsentException("Consent status: EXPIRED");
        }
        checkAisConsentCounter(aisConsent.getUsageCounter());
        AisConsent updated = updateConsentStatusAndCounter(aisConsent);
        Map<String, Set<AccessAccountInfo>> targetAccounts = consentMapper.toMap(updated.getAccounts());
        Map<String, Set<AccessAccountInfo>> requestedAccounts = request.getAccountsAccesses();
        return filterRequestedAccounts(targetAccounts, requestedAccounts);
    }

    private void checkAisConsentCounter(int usageCounter){
        if(usageCounter == 0){
            throw new ConsentException("Limit of usage is exceeded");
        }
    }

    private AisConsent updateConsentStatusAndCounter(AisConsent aisConsent) {
        if (aisConsent.getConsentStatus() == RECEIVED) {
            aisConsent.setConsentStatus(VALID);
        }
        int usageCounter = aisConsent.getUsageCounter();
        int newUsageCounter = --usageCounter;
        aisConsent.setUsageCounter(newUsageCounter);
        return aisConsentRepository.save(aisConsent);
    }

    private Map<String, Set<AccessAccountInfo>> filterRequestedAccounts(Map<String, Set<AccessAccountInfo>> targetAccounts, Map<String, Set<AccessAccountInfo>> requestedAccounts) {
        return requestedAccounts.entrySet().stream()
                   .filter(e -> targetAccounts.containsKey(e.getKey()))
                   .collect(Collectors.toMap(Map.Entry::getKey, e -> filterAccessAccount(e.getValue(), targetAccounts.get(e.getKey()))));
    }

    private Set<AccessAccountInfo> filterAccessAccount(Set<AccessAccountInfo> incomeAccess, Set<AccessAccountInfo> bankAccess) {
        Collection<AccessAccountInfo> subtract = CollectionUtils.subtract(incomeAccess, bankAccess);
        incomeAccess.removeAll(subtract);
        return incomeAccess;
    }
    */

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq request, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        String tppId = "This is a test TppId"; //TODO to clarify where it should get from
        CreateConsentReq checkedRequest = new CreateConsentReq();
        if (isNotEmptyAccountAccess(request.getAccess())) {
            if (isAllAccountsRequest(request) && psuId != null) {
                checkedRequest.setAccess(getAccessByPsuId(AccountAccessType.ALL_ACCOUNTS.equals(request.getAccess().getAllPsd2()), psuId));
            } else {
                checkedRequest.setAccess(getAccessByRequestedAccess(request.getAccess()));
            }
            checkedRequest.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
            checkedRequest.setRecurringIndicator(request.isRecurringIndicator());
            checkedRequest.setFrequencyPerDay(request.getFrequencyPerDay()); //TODO Check This with special microservice byRoman
            checkedRequest.setValidUntil(request.getValidUntil());

        }
        String consentId = isNotEmptyAccountAccess(checkedRequest.getAccess())
                               ? aisConsentService.createConsent(consentMapper.mapToAisConsentRequest(checkedRequest, psuId, tppId))
                               : null;
        //TODO v1.1 Add balances support
        return !StringUtils.isBlank(consentId)
                   ? ResponseObject.<CreateConsentResp>builder().body(new CreateConsentResp(ConsentStatus.RECEIVED, consentId, null, null, null)).build()
                   : ResponseObject.<CreateConsentResp>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR))).build();
    }

    private AccountAccess getAccessByRequestedAccess(AccountAccess requestedAccess) {
        List<AccountReference> refs = accountMapper.mapToAccountReferencesFromDetails(accountSpi.readAccountDetailsByIbans(getIbansFromAccess(requestedAccess)));
        if (refs.isEmpty()) {
            return null;
        }
        List<AccountReference> balances = getAccountReference(requestedAccess.getBalances(), refs);
        List<AccountReference> transaction = getRequestedReferences(requestedAccess.getTransactions(), refs);
        List<AccountReference> accounts = getRequestedReferences(requestedAccess.getAccounts(), refs);

        return new AccountAccess(getAccountsForAccess(balances, transaction, accounts), balances, transaction, null, null);
    }

    private List<AccountReference> getAccountReference(List<AccountReference> requestedReferences, List<AccountReference> refs) {
        return Optional.ofNullable(requestedReferences)
                                              .map(reqRefs -> getRequestedReferences(reqRefs, refs))
                                              .orElse(Collections.emptyList());
    }

    private List<AccountReference> getAccountsForAccess(List<AccountReference> balances, List<AccountReference> transactions, List<AccountReference> accounts) {
        accounts.removeAll(balances);
        accounts.addAll(balances);
        accounts.removeAll(transactions);
        accounts.addAll(transactions);
        return accounts;
    }

    private List<AccountReference> getRequestedReferences(List<AccountReference> requestedRefs, List<AccountReference> refs) {
        return Optional.ofNullable(requestedRefs).map(rr->rr.stream()
                   .filter(r -> isContainedRefinRefsList(r, refs))
                   .collect(Collectors.toList())).orElse(Collections.emptyList());
    }

    private boolean isContainedRefinRefsList(AccountReference referenceMatched, List<AccountReference> references) {
        return references.stream()
                   .anyMatch(r -> referenceMatches(r, referenceMatched));
    }

    private boolean referenceMatches(AccountReference referenceMatcher, AccountReference referenceMatched) {
        return referenceMatched.getCurrency() == null
                   ? referenceMatcher.getIban().equals(referenceMatched.getIban())
                   : referenceMatcher.getIban().equals(referenceMatched.getIban())
                         && referenceMatcher.getCurrency().equals(referenceMatched.getCurrency());
    }

    private AccountAccess getAccessByPsuId(boolean isAllPSD2, String psuId) {
        List<AccountReference> refs = accountMapper.mapToAccountReferencesFromDetails(accountSpi.readAccountsByPsuId(psuId));

        return isAllPSD2
                   ? new AccountAccess(refs, refs, refs, null, AccountAccessType.ALL_ACCOUNTS)
                   : new AccountAccess(refs, Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null);
    }

    private boolean isAllAccountsRequest(CreateConsentReq request) {
        return Optional.ofNullable(request.getAccess())
                   .filter(a -> AccountAccessType.ALL_ACCOUNTS.equals(a.getAllPsd2())
                                    || AccountAccessType.ALL_ACCOUNTS.equals(a.getAvailableAccounts())).isPresent();
    }

    public ResponseObject<ConsentStatus> getAccountConsentsStatusById(String consentId) {
        return consentMapper.mapToConsentStatus(consentSpi.getAccountConsentStatusById(consentId))
                   .map(status -> ResponseObject.<ConsentStatus>builder().body(status).build())
                   .orElse(ResponseObject.<ConsentStatus>builder()
                               .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404)))
                               .build());
    }

    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        if (consentSpi.getAccountConsentById(consentId) != null) {
            consentSpi.deleteAccountConsentById(consentId);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        AccountConsent consent = consentMapper.mapToAccountConsent(consentSpi.getAccountConsentById(consentId));
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    public Map<String, Set<AccessAccountInfo>> checkValidityByConsent(String consentId, List<AccountDetails> details, TypeAccess typeAccess, boolean withBalance) {
        AvailableAccessRequest request = new AvailableAccessRequest();
        request.setConsentId(consentId);

        Set<String> ibans = details.stream()
                                .map(AccountDetails::getIban)
                                .collect(Collectors.toSet());
        Map<String, Set<AccessAccountInfo>> accesses = ibans.stream()
                                                           .collect(Collectors.toMap(iban -> iban,
                                                               iban -> getAccessAccountInfoByIban(details, typeAccess, withBalance, iban)));
        request.setAccountsAccesses(accesses);
        return consentSpi.checkValidityByConsent(request);
    }

    private Set<AccessAccountInfo> getAccessAccountInfoByIban(List<AccountDetails> details, TypeAccess typeAccess, boolean withBalance, String iban) {
        return details.stream().filter(a -> a.getIban().equals(iban))
                   .flatMap(d -> getAccessAccountInfo(d.getCurrency(), typeAccess, withBalance)
                                     .stream())
                   .collect(Collectors.toSet());
    }

    private Set<AccessAccountInfo> getAccessAccountInfo(Currency currency, TypeAccess typeAccess, boolean withBalance) {
        Set<AccessAccountInfo> set = new HashSet<>();
        set.add(new AccessAccountInfo(currency.getCurrencyCode(), typeAccess));
        set.add(new AccessAccountInfo(currency.getCurrencyCode(), TypeAccess.ACCOUNT));
        if (withBalance) {
            set.add(new AccessAccountInfo(currency.getCurrencyCode(), TypeAccess.BALANCE));
        }
        return set;
    }

    public boolean isValidAccountByAccess(String iban, Currency currency, TypeAccess typeAccess, Map<String, Set<AccessAccountInfo>> allowedAccountData) {
        Set<AccessAccountInfo> accesses = Optional.ofNullable(allowedAccountData.get(iban))
                                              .orElse(Collections.emptySet());
        return accesses.contains(new AccessAccountInfo(currency.getCurrencyCode(), typeAccess));
    }

    public Set<String> getIbanSetFromAccess(AccountAccess access) {
        if (isNotEmptyAccountAccess(access)) {
            return getIbansFromAccess(access);
        }
        return Collections.emptySet();
    }

    public Set<String> getIbansFromAccountReference(List<AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(list -> list.stream()
                                    .map(AccountReference::getIban)
                                    .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
    }

    private Set<String> getIbansFromAccess(AccountAccess access) {
        return Stream.of(
            getIbansFromAccountReference(access.getAccounts()),
            getIbansFromAccountReference(access.getBalances()),
            getIbansFromAccountReference(access.getTransactions())
        )
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private boolean isNotEmptyAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access).filter(a->!(CollectionUtils.isEmpty(a.getAccounts())
                     && CollectionUtils.isEmpty(a.getBalances())
                     && CollectionUtils.isEmpty(a.getTransactions())
                     && a.getAllPsd2() == null
                     && a.getAvailableAccounts() == null)).isPresent();
    }
}
