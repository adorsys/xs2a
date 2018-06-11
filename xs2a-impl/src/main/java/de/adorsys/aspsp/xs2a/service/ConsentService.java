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
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccessAccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AvailableAccessRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.TypeAccess;
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

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        String tppId = "This is a test TppId"; //TODO to clarify where it should get from
        String consentId = aisConsentService.createConsent(consentMapper.mapToAisConsentRequest(createAccountConsentRequest, psuId, tppId));
        //TODO v1.1 Add balances support
        return !StringUtils.isBlank(consentId)
                   ? ResponseObject.<CreateConsentResp>builder().body(new CreateConsentResp(ConsentStatus.RECEIVED, consentId, null, null, null)).build()
                   : ResponseObject.<CreateConsentResp>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.FORMAT_ERROR))).build();
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
        return !(CollectionUtils.isEmpty(access.getAccounts())
                     && CollectionUtils.isEmpty(access.getBalances())
                     && CollectionUtils.isEmpty(access.getTransactions())
                     && access.getAllPsd2() == null
                     && access.getAvailableAccounts() == null);
    }
}
