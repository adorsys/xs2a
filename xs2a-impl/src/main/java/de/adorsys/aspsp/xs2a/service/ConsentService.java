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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.consent.ais.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus.RECEIVED;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final ConsentMapper consentMapper;
    private final AisConsentService aisConsentService;
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;
    private final AspspProfileService aspspProfileService;

    /**
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuId   String representing PSU identification at ASPSP
     * @return CreateConsentResponse representing the complete response to create consent request
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, String psuId) {
        if (isNotBankOfferConsentSupported(request)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
        }
        CreateConsentReq checkedRequest = new CreateConsentReq();
        if (isNotEmptyAccess(request.getAccess())) {
            if (!isValidExpirationDate(request.getValidUntil())) {
                return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PERIOD_INVALID))).build();
            }

            if (isNotSupportedGlobalConsentForAllPsd2(request)) {
                return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
            }

            checkedRequest.setAccess(getNecessaryAccess(request, psuId));
            checkedRequest.setCombinedServiceIndicator(request.isCombinedServiceIndicator());
            checkedRequest.setRecurringIndicator(request.isRecurringIndicator());
            checkedRequest.setFrequencyPerDay(request.getFrequencyPerDay());
            checkedRequest.setValidUntil(request.getValidUntil());
        }
        String tppId = "This is a test TppId"; //TODO v1.1 add corresponding request header
        String consentId = isNotEmptyAccess(checkedRequest.getAccess())
                               ? aisConsentService.createConsent(checkedRequest, psuId, tppId)
                               : null;
        //TODO v1.1 Add balances support
        return !StringUtils.isBlank(consentId)
                   ? ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(RECEIVED.getValue(), consentId, null, null)).build()
                   : ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_400))).build();
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     * Returns status of requested consent
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        return consentMapper.mapToConsentStatus(aisConsentService.getAccountConsentStatusById(consentId))
                   .map(status -> ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(status)).build())
                   .orElseGet(() -> ResponseObject.<ConsentStatusResponse>builder()
                                        .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400)))
                                        .build());
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     * Revokes account consent on PSU request
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        if (aisConsentService.getAccountConsentById(consentId) != null) {
            aisConsentService.revokeConsent(consentId);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        AccountConsent consent = consentMapper.mapToAccountConsent(aisConsentService.getAccountConsentById(consentId));
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    ResponseObject<AccountAccess> getValidatedConsent(String consentId) {
        AccountConsent consent = consentMapper.mapToAccountConsent(aisConsentService.getAccountConsentById(consentId));
        if (consent == null) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
        }
        if (!consent.isValidStatus()) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }
        if (!consent.isValidFrequency()) {
            return ResponseObject.<AccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.ACCESS_EXCEEDED))).build();
        }
        return ResponseObject.<AccountAccess>builder().body(consent.getAccess()).build();
    }

    boolean isValidAccountByAccess(String iban, Currency currency, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getIban().equals(iban)
                                             && a.getCurrency() == currency);
    }

    private boolean isValidExpirationDate(LocalDate validUntil) {
        int consentLifetime = Math.abs(aspspProfileService.getConsentLifetime());
        return validUntil.isAfter(LocalDate.now()) && isValidConsentLifetime(consentLifetime, validUntil);
    }

    private boolean isValidConsentLifetime(int consentLifetime, LocalDate validUntil) {
        return consentLifetime == 0 || validUntil.isBefore(LocalDate.now().plusDays(consentLifetime));
    }

    private AccountAccess getNecessaryAccess(CreateConsentReq request, String psuId) {
        return isAllAccountsRequest(request) && psuId != null
                   ? getAccessByPsuId(AccountAccessType.ALL_ACCOUNTS == request.getAccess().getAllPsd2(), psuId)
                   : getAccessByRequestedAccess(request.getAccess());
    }

    private Set<String> getIbansFromAccountReference(List<AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(list -> list.stream()
                                    .map(AccountReference::getIban)
                                    .collect(Collectors.toSet()))
                   .orElseGet(Collections::emptySet);
    }

    private Boolean isNotEmptyAccess(AccountAccess access) {
        return Optional.ofNullable(access)
                   .map(AccountAccess::isNotEmpty)
                   .orElse(false);
    }

    private AccountAccess getAccessByRequestedAccess(AccountAccess requestedAccess) {
        Set<String> ibansFromAccess = getIbansFromAccess(requestedAccess);
        List<SpiAccountDetails> accountDetailsList = accountSpi.readAccountDetailsByIbans(
            ibansFromAccess,
            new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload();
        List<AccountReference> aspspReferences = accountMapper.mapToAccountReferencesFromDetails(accountDetailsList); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        List<AccountReference> balances = getFilteredReferencesByAccessReferences(requestedAccess.getBalances(), aspspReferences);
        List<AccountReference> transaction = getRequestedReferences(requestedAccess.getTransactions(), aspspReferences);
        List<AccountReference> accounts = getRequestedReferences(requestedAccess.getAccounts(), aspspReferences);
        return new AccountAccess(getAccountsForAccess(balances, transaction, accounts), balances, transaction, null, null);
    }

    private List<AccountReference> getFilteredReferencesByAccessReferences(List<AccountReference> requestedReferences, List<AccountReference> refs) {
        return Optional.ofNullable(requestedReferences)
                   .map(reqRefs -> getRequestedReferences(reqRefs, refs))
                   .orElseGet(Collections::emptyList);
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
                                                                .filter(r -> isContainedRefInRefsList(r, refs))
                                                                .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private boolean isContainedRefInRefsList(AccountReference referenceMatched, List<AccountReference> references) {
        return references.stream()
                   .anyMatch(r -> r.matches(referenceMatched));
    }

    private AccountAccess getAccessByPsuId(boolean isAllPSD2, String psuId) {
        List<AccountReference> refs = accountMapper.mapToAccountReferencesFromDetails(accountSpi.readAccountsByPsuId(psuId, new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload()); // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Put a real data here
        if (CollectionUtils.isNotEmpty(refs)) {
            return isAllPSD2
                       ? new AccountAccess(refs, refs, refs, null, AccountAccessType.ALL_ACCOUNTS)
                       : new AccountAccess(refs, Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null);
        } else {
            return new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);
        }
    }

    private boolean isAllAccountsRequest(CreateConsentReq request) {
        return Optional.ofNullable(request.getAccess())
                   .filter(a -> AccountAccessType.ALL_ACCOUNTS == a.getAllPsd2()
                                    || AccountAccessType.ALL_ACCOUNTS == a.getAvailableAccounts()).isPresent();
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

    private boolean isNotSupportedGlobalConsentForAllPsd2(CreateConsentReq request) {
        return request.getAccess().getAllPsd2() == AccountAccessType.ALL_ACCOUNTS &&
            CollectionUtils.isEmpty(request.getAccountReferences())
                   && !aspspProfileService.getAllPsd2Support();
    }

    private boolean isNotBankOfferConsentSupported(CreateConsentReq request) {
        return !aspspProfileService.isBankOfferedConsentSupported()
                   && !isNotEmptyAccess(request.getAccess());
    }
}
