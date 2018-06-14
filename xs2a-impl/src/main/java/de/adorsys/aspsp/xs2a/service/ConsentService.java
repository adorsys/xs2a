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

import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus.RECEIVED;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus.VALID;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final ConsentMapper consentMapper;
    private final AisConsentService aisConsentService;
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;

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
            checkedRequest.setFrequencyPerDay(request.getFrequencyPerDay());
            checkedRequest.setValidUntil(request.getValidUntil());

        }
        String consentId = isNotEmptyAccountAccess(checkedRequest.getAccess())
                               ? aisConsentService.createConsent(checkedRequest, psuId, tppId)
                               : null;
        //TODO v1.1 Add balances support
        return !StringUtils.isBlank(consentId)
                   ? ResponseObject.<CreateConsentResp>builder().body(new CreateConsentResp(RECEIVED, consentId, null, null, null)).build()
                   : ResponseObject.<CreateConsentResp>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR))).build();
    }

    public ResponseObject<ConsentStatus> getAccountConsentsStatusById(String consentId) {
        return consentMapper.mapToConsentStatus(aisConsentService.getAccountConsentStatusById(consentId))
                   .map(status -> ResponseObject.<ConsentStatus>builder().body(status).build())
                   .orElse(ResponseObject.<ConsentStatus>builder()
                               .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404)))
                               .build());
    }

    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        if (aisConsentService.getAccountConsentById(consentId) != null) {
            aisConsentService.revokeConsent(consentId);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build();
    }

    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        AccountConsent consent = consentMapper.mapToAccountConsent(aisConsentService.getAccountConsentById(consentId));
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    public ResponseObject<AccountAccess> getValidatedConsent(String consentId) {
        AccountConsent consent = consentMapper.mapToAccountConsent(aisConsentService.getAccountConsentById(consentId));
        if (consent == null) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_403))).build();
        }
        if (!EnumSet.of(VALID, RECEIVED).contains(consent.getConsentStatus())
                || consent.getFrequencyPerDay() < 1
                || consent.getValidUntil().compareTo(new Date()) <= 0) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }
        return ResponseObject.<AccountAccess>builder().body(consent.getAccess()).build();
    }

    public boolean isValidAccountByAccess(String iban, Currency currency, List<AccountReference> allowedAccountData) {

        return Optional.ofNullable(allowedAccountData)
                   .map(allowed -> allowed.stream()
                                       .anyMatch(a -> a.getIban().equals(iban)
                                                          && a.getCurrency().equals(currency)))
                   .orElse(false);
    }

    public Set<String> getIbansFromAccountReference(List<AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(list -> list.stream()
                                    .map(AccountReference::getIban)
                                    .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
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
        return Optional.ofNullable(requestedRefs).map(rr -> rr.stream()
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
        return Optional.ofNullable(access).filter(a -> !(CollectionUtils.isEmpty(a.getAccounts())
                                                             && CollectionUtils.isEmpty(a.getBalances())
                                                             && CollectionUtils.isEmpty(a.getTransactions())
                                                             && a.getAllPsd2() == null
                                                             && a.getAvailableAccounts() == null)).isPresent();
    }
}
