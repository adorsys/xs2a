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
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.isEmpty;

@AllArgsConstructor
@Service
public class ConsentService {
    private String consentsLinkRedirectToSource;
    private ConsentSpi consentSpi;
    private ConsentMapper consentMapper;
    private AccountService accountService;

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        Optional<String> consentId = createAccountConsentsAndReturnId(createAccountConsentRequest, withBalance, tppRedirectPreferred, psuId);
        return consentId.isPresent()
                   ? ResponseObject.builder().body(new CreateConsentResp(TransactionStatus.RCVD, consentId.get(), null, getLinkToConsent(consentId.get()), null)).build()
                   : ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.FORMAT_ERROR))).build();
    }

    public ResponseObject<TransactionStatus> getAccountConsentsStatusById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
                   ? ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.builder().body(consent.getTransactionStatus()).build();
    }

    public ResponseObject<AccountConsent> getAccountConsentsById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
                   ? ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
                   : ResponseObject.builder().body(consent).build();
    }

    public ResponseObject<Boolean> deleteAccountConsentsById(String consentId) {
        boolean present = getAccountConsentsById(consentId).getBody() != null;
        if (present) {
            consentSpi.deleteAccountConsentsById(consentId);
        }

        return present
                   ? ResponseObject.builder().body(true).build()
                   : ResponseObject.builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build();
    }

    private Optional<String> createAccountConsentsAndReturnId(CreateConsentReq req, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        Optional<AccountAccess> access = Optional.ofNullable(createAccountAccess(req.getAccess(), psuId));
        if (req.isRecurringIndicator()&&access.isPresent()){
            consentSpi.expireConsent(consentMapper.mapToSpiAccountAccess(access.get()));
        }
        return access.map(accountAccess -> saveAccountConsent(
            new AccountConsent(null, accountAccess, req.isRecurringIndicator(), req.getValidUntil(), req.getFrequencyPerDay(),
                null, TransactionStatus.ACCP, ConsentStatus.VALID, withBalance, tppRedirectPreferred)));
    }

    private String saveAccountConsent(AccountConsent consent) {
        return consentSpi.createAccountConsents(consentMapper.mapToSpiAccountConsent(consent));
    }

    private AccountAccess createAccountAccess(AccountAccess access, String psuId) {
        if (isAllAccountsOrAllPsd2(access, psuId)) {
            return createAccessByPsuId(access, psuId);
        }

        return createAccessByIbans(access);
    }

    private AccountAccess createAccessByIbans(AccountAccess access) {
        Set<String> ibans = getIbanSetFromAccess(access);
        List<AccountDetails> accountDetails = accountService.getAccountDetailsListByIbans(ibans);
        Map<String, AccountReference> referenceMap = Optional.ofNullable(accountDetails)
                                                         .map(ad -> getAccountReferencesMap(getArrayOfReferenses(ad)))
                                                         .orElse(null);
        Set<String> balances = getIbansFromAccountReference(access.getBalances(), true);
        Set<String> transactions = getIbansFromAccountReference(access.getTransactions(), true);
        Set<String> accounts = fillUpdateAccountsWithAdditionalReferencesFromBalancesAndTransactions(getIbansFromAccountReference(access.getAccounts(), true), balances, transactions);

        AccountAccess resultAccess = setAccountAccess(getAccountReferencesFromMap(referenceMap, accounts), getAccountReferencesFromMap(referenceMap, accounts),
            getAccountReferencesFromMap(referenceMap, accounts), null, null);

        return isNotEmptyAccountAccess(resultAccess)
                   ? resultAccess : null;
    }

    private AccountReference[] getAccountReferencesFromMap(Map<String, AccountReference> referenceMap, Set<String> accounts) {
        return Optional.ofNullable(accounts).map(acc -> accounts.stream().map(referenceMap::get).toArray(AccountReference[]::new)).orElse(new AccountReference[]{});
    }

    private Set<String> fillUpdateAccountsWithAdditionalReferencesFromBalancesAndTransactions(Set<String> accounts, Set<String> balances, Set<String> transactions) {

        return Stream.of(accounts.stream(), balances.stream(), transactions.stream())
                   .flatMap(stringStream -> stringStream)
                   .collect(Collectors.toSet());
    }

    private Map<String, AccountReference> getAccountReferencesMap(AccountReference[] ar) {
        return Arrays.stream(ar)
                   .collect(Collectors.toMap(this::mapUniqueAccountIdentifier, Function.identity()));
    }

    private String mapUniqueAccountIdentifier(AccountReference reference) {
        return reference.getIban() + reference.getCurrency().getDisplayName();
    }

    private AccountAccess createAccessByPsuId(AccountAccess access, String psuId) {
        List<AccountDetails> accountDetails = accountService.getAccountDetailsByPsuId(psuId);
        AccountReference[] references = Optional.ofNullable(accountDetails)
                                            .map(this::getArrayOfReferenses)
                                            .orElse(null);
        return Optional.ofNullable(references)
                   .map(ref -> setAccountAccess(ref,
                       isAllPsd2(access) ? ref : null,
                       isAllPsd2(access) ? ref : null,
                       isAllPsd2(access) ? null : AccountAccessType.ALL_ACCOUNTS,
                       isAllPsd2(access) ? AccountAccessType.ALL_ACCOUNTS : null))
                   .orElse(null);
    }

    private AccountReference[] getArrayOfReferenses(List<AccountDetails> ad) {
        return ad.isEmpty() ? null : ad.stream().map(this::mapFromAccountDetails).toArray(AccountReference[]::new);
    }

    private AccountAccess setAccountAccess(AccountReference[] accounts, AccountReference[] balances,
                                           AccountReference[] transactions, AccountAccessType availableAccounts,
                                           AccountAccessType allPsd2) {
        AccountAccess acc = new AccountAccess();
        acc.setAccounts(accounts);
        acc.setBalances(balances);
        acc.setTransactions(transactions);
        acc.setAvailableAccounts(availableAccounts);
        acc.setAllPsd2(allPsd2);
        return acc;
    }

    private Set<String> getIbanSetFromAccess(AccountAccess access) {
        return Optional.ofNullable(access)
                   .filter(this::isNotEmptyAccountAccess)
                   .map(this::getIbansFromAccess)
                   .orElseGet(HashSet::new);
    }

    private Set<String> getIbansFromAccess(AccountAccess access) {
        Set<String> ibans = new HashSet<>();
        getIbansFromAccountReference(Optional.ofNullable(access.getAccounts()).orElse(null), false).addAll(ibans);
        getIbansFromAccountReference(Optional.ofNullable(access.getBalances()).orElse(null), false).addAll(ibans);
        getIbansFromAccountReference(Optional.ofNullable(access.getTransactions()).orElse(null), false).addAll(ibans);
        return ibans;
    }

    private Set<String> getIbansFromAccountReference(AccountReference[] references, boolean withCurrncy) {
        return Arrays.stream(references)
                   .map(ar -> withCurrncy ? mapUniqueAccountIdentifier(ar) : ar.getIban())
                   .collect(Collectors.toSet());
    }

    private boolean isNotEmptyAccountAccess(AccountAccess access) {
        return !(ArrayUtils.isEmpty(access.getAccounts())
                     && ArrayUtils.isEmpty(access.getBalances())
                     && ArrayUtils.isEmpty(access.getTransactions())
                     && access.getAllPsd2() == null
                     && access.getAvailableAccounts() == null);
    }

    private boolean isAllAccountsOrAllPsd2(AccountAccess access, String psuId) {
        if (psuId != null) {
            if (Optional.ofNullable(access).isPresent()) {
                return access.getAvailableAccounts() == AccountAccessType.ALL_ACCOUNTS || access.getAllPsd2() == AccountAccessType.ALL_ACCOUNTS;
            }

        }
        return false;
    }

    private AccountReference mapFromAccountDetails(AccountDetails details) {
        AccountReference reference = new AccountReference();
        reference.setAccountId(details.getId());
        reference.setIban(details.getIban());
        reference.setBban(details.getBban());
        reference.setPan(details.getPan());
        reference.setMaskedPan(details.getMaskedPan());
        reference.setMsisdn(details.getMsisdn());
        reference.setCurrency(details.getCurrency());
        return reference;
    }

    private boolean isAllPsd2(AccountAccess access) {
        return access.getAllPsd2() == AccountAccessType.ALL_ACCOUNTS;
    }

    private Links getLinkToConsent(String consentId) {
        Links linksToConsent = new Links();

        // Response in case of the OAuth2 approach
        // todo figure out when we should return  OAuth2 response
        //String selfLink = linkTo(ConsentInformationController.class).slash(consentId).toString();
        //linksToConsent.setSelf(selfLink);

        // Response in case of a redirect
        String redirectLink = consentsLinkRedirectToSource + "/" + consentId;
        linksToConsent.setRedirect(redirectLink);

        return linksToConsent;
    }
}
