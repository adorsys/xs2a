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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccessType;
import de.adorsys.aspsp.xs2a.domain.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class ConsentModelMapper {
    private final ObjectMapper objectMapper;

    public CreateConsentReq mapToCreateConsentReq(Consents consent) {
        return Optional.ofNullable(consent)
                   .map(cnst -> {
                       CreateConsentReq createAisConsentRequest = new CreateConsentReq();
                       createAisConsentRequest.setAccess(mapToAccountAccessInner(cnst.getAccess()));
                       createAisConsentRequest.setRecurringIndicator(cnst.getRecurringIndicator());
                       createAisConsentRequest.setValidUntil(cnst.getValidUntil());
                       createAisConsentRequest.setFrequencyPerDay(cnst.getFrequencyPerDay());
                       createAisConsentRequest.setCombinedServiceIndicator(cnst.isCombinedServiceIndicator());

                       return createAisConsentRequest;
                   })
                   .orElse(null);
    }

    public ResponseObject mapToConsentsResponse201ResponseObject(ResponseObject<CreateConsentResponse> createConsentResponse) {
        if (!createConsentResponse.hasError()) {
            return ResponseObject.builder().body(mapToConsentsResponse201(createConsentResponse.getBody())).build();
        }
        return createConsentResponse;
    }

    public ResponseObject mapToConsentStatusResponse200ResponseObject(ResponseObject<ConsentStatusResponse> consentStatusResponse) {
        if (!consentStatusResponse.hasError()) {
            return ResponseObject.builder().body(mapToConsentStatusResponse200(consentStatusResponse.getBody())).build();
        }
        return consentStatusResponse;
    }

    private ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
                   .map(cstr -> new ConsentStatusResponse200().consentStatus(ConsentStatus.fromValue(cstr.getConsentStatus())))
                   .orElse(null);
    }

    public ResponseObject mapToConsentInformationResponse200JsonResponseObject(ResponseObject<AccountConsent> accountConsentResponseObject) {
        if (!accountConsentResponseObject.hasError()) {
            return ResponseObject.builder().body(mapToConsentInformationResponse200Json(accountConsentResponseObject.getBody())).build();
        }

        return accountConsentResponseObject;
    }

    private ConsentsResponse201 mapToConsentsResponse201(CreateConsentResponse createConsentResponse) {
        return Optional.ofNullable(createConsentResponse)
                   .map(cnst ->
                            new ConsentsResponse201()
                                .consentStatus(ConsentStatus.fromValue(cnst.getConsentStatus()))
                                .consentId(cnst.getConsentId())
                                .scaMethods(mapToScaMethodsOuter(cnst))
                                ._links(objectMapper.convertValue(cnst.getLinks(), Map.class))
                                .message(cnst.getPsuMessage())
                   )
                   .orElse(null);
    }

    private ConsentInformationResponse200Json mapToConsentInformationResponse200Json(AccountConsent accountConsent) {
        return Optional.ofNullable(accountConsent)
                   .map(consent ->
                            new ConsentInformationResponse200Json()
                                .access(mapToAccountAccessDomain(consent.getAccess()))
                                .recurringIndicator(consent.isRecurringIndicator())
                                .validUntil(consent.getValidUntil())
                                .frequencyPerDay(consent.getFrequencyPerDay())
                                .lastActionDate(consent.getLastActionDate())
                                .consentStatus(ConsentStatus.fromValue(consent.getConsentStatus().getValue()))
                   )
                   .orElse(null);
    }

    private ScaMethods mapToScaMethodsOuter(CreateConsentResponse createConsentResponse) {
        List<AuthenticationObject> authList = Optional.ofNullable(createConsentResponse.getScaMethods())
                                                  .map(arr -> Arrays.stream(createConsentResponse.getScaMethods())
                                                                  .map(au -> new AuthenticationObject()
                                                                                 .authenticationType(AuthenticationType.fromValue(au.getAuthenticationType().getDescription()))
                                                                                 .authenticationVersion(au.getAuthenticationVersion())
                                                                                 .authenticationMethodId(au.getAuthenticationMethodId())
                                                                                 .name(au.getName())
                                                                                 .explanation(au.getExplanation()))
                                                                  .collect(Collectors.toList()))
                                                  .orElse(Collections.emptyList());
        ScaMethods scaMethods = new ScaMethods();
        scaMethods.addAll(authList);

        return scaMethods;
    }

    private de.adorsys.aspsp.xs2a.domain.consent.AccountAccess mapToAccountAccessInner(AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
                   .map(acs ->
                            new de.adorsys.aspsp.xs2a.domain.consent.AccountAccess(
                                mapToAccountReferencesInner(acs.getAccounts()),
                                mapToAccountReferencesInner(acs.getBalances()),
                                mapToAccountReferencesInner(acs.getTransactions()),
                                mapToAccountAccessTypeFromAvailableAccounts(acs.getAvailableAccounts()),
                                mapToAccountAccessTypeFromAllPsd2Enum(acs.getAllPsd2())
                            ))
                   .orElse(null);
    }

    private AccountAccess mapToAccountAccessDomain(de.adorsys.aspsp.xs2a.domain.consent.AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
                   .map(access -> {
                           AccountAccess mappedAccountAccess = new AccountAccess();

                           mappedAccountAccess.setAccounts(new ArrayList<>(access.getAccounts()));
                           mappedAccountAccess.setBalances(new ArrayList<>(access.getBalances()));
                           mappedAccountAccess.setTransactions(new ArrayList<>(access.getTransactions()));
                           mappedAccountAccess.setAvailableAccounts(
                               AccountAccess.AvailableAccountsEnum.fromValue(
                                   Optional.ofNullable(access.getAvailableAccounts())
                                       .map(AccountAccessType::getDescription)
                                       .orElse(null)
                               )
                           );
                           mappedAccountAccess.setAllPsd2(
                               AccountAccess.AllPsd2Enum.fromValue(
                                   Optional.ofNullable(access.getAllPsd2())
                                       .map(AccountAccessType::getDescription)
                                       .orElse(null)
                               )
                           );

                           return mappedAccountAccess;
                       }
                   )
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccounts(AccountAccess.AvailableAccountsEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElseGet(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElseGet(null);
    }

    private List<AccountReference> mapToAccountReferencesInner(List<Object> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToAccountReferenceInner)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private AccountReference mapToAccountReferenceInner(Object reference) {
        return objectMapper.convertValue(reference, AccountReference.class);
    }

    private AccountReference getAccountReference(String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency) {
        AccountReference reference = new AccountReference();
        reference.setIban(iban);
        reference.setBban(bban);
        reference.setPan(pan);
        reference.setMaskedPan(maskedPan);
        reference.setMsisdn(msisdn);
        reference.setCurrency(currency);
        return reference;
    }

    private Currency getCurrencyByCode(String code) {
        return Optional.ofNullable(code)
                   .map(Currency::getInstance)
                   .orElseGet(null);
    }
}
