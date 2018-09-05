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
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AuthenticationObject;
import de.adorsys.psd2.model.AuthenticationType;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.model.ConsentStatus;
import org.apache.commons.lang3.BooleanUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ConsentModelMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static CreateConsentReq mapToCreateConsentReq(Consents consent) {
        return Optional.ofNullable(consent)
            .map(cnst -> {
                CreateConsentReq createAisConsentRequest = new CreateConsentReq();
                createAisConsentRequest.setAccess(mapToAccountAccessInner(cnst.getAccess()));
                createAisConsentRequest.setRecurringIndicator(cnst.getRecurringIndicator());
                createAisConsentRequest.setValidUntil(cnst.getValidUntil());
                createAisConsentRequest.setFrequencyPerDay(cnst.getFrequencyPerDay());
                createAisConsentRequest.setCombinedServiceIndicator(BooleanUtils.toBoolean(cnst.isCombinedServiceIndicator()));

                return createAisConsentRequest;
            })
            .orElse(null);
    }

    public static ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
            .map(cstr -> new ConsentStatusResponse200().consentStatus(ConsentStatus.fromValue(cstr.getConsentStatus())))
            .orElse(null);
    }

    public static ConsentsResponse201 mapToConsentsResponse201(CreateConsentResponse createConsentResponse) {
        return Optional.ofNullable(createConsentResponse)
            .map(cnst ->
                new ConsentsResponse201()
                    .consentStatus(ConsentStatus.fromValue(cnst.getConsentStatus()))
                    .consentId(cnst.getConsentId())
                    .scaMethods(mapToScaMethodsOuter(cnst))
                    ._links(OBJECT_MAPPER.convertValue(cnst.getLinks(), Map.class))
                    .message(cnst.getPsuMessage())
            )
            .orElse(null);
    }

    public static ConsentInformationResponse200Json mapToConsentInformationResponse200Json(AccountConsent accountConsent) {
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

    private static ScaMethods mapToScaMethodsOuter(CreateConsentResponse createConsentResponse) {
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

    private static de.adorsys.aspsp.xs2a.domain.consent.AccountAccess mapToAccountAccessInner(AccountAccess accountAccess) {
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

    private static AccountAccess mapToAccountAccessDomain(de.adorsys.aspsp.xs2a.domain.consent.AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
            .map(access -> {
                    AccountAccess mappedAccountAccess = new AccountAccess();

                    mappedAccountAccess.setAccounts(new ArrayList<>(access.getAccounts()));
                    mappedAccountAccess.setBalances(new ArrayList<>(access.getBalances()));
                    mappedAccountAccess.setTransactions(new ArrayList<>(access.getTransactions()));
                    mappedAccountAccess.setAvailableAccounts(
                        AccountAccess.AvailableAccountsEnum.fromValue(
                            Optional.ofNullable(access.getAvailableAccounts())
                                .map(Xs2aAccountAccessType::getDescription)
                                .orElse(null)
                        )
                    );
                    mappedAccountAccess.setAllPsd2(
                        AccountAccess.AllPsd2Enum.fromValue(
                            Optional.ofNullable(access.getAllPsd2())
                                .map(Xs2aAccountAccessType::getDescription)
                                .orElse(null)
                        )
                    );

                    return mappedAccountAccess;
                }
            )
            .orElse(null);
    }

    private static Xs2aAccountAccessType mapToAccountAccessTypeFromAvailableAccounts(AccountAccess.AvailableAccountsEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
            .flatMap(en -> Xs2aAccountAccessType.getByDescription(en.toString()))
            .orElse(null);
    }

    private static Xs2aAccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
            .flatMap(en -> Xs2aAccountAccessType.getByDescription(en.toString()))
            .orElse(null);
    }

    private static List<AccountReference> mapToAccountReferencesInner(List<Object> references) {
        return Optional.ofNullable(references)
            .map(ref -> ref.stream()
                .map(ConsentModelMapper::mapToAccountReferenceInner)
                .collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
    }

    private static AccountReference mapToAccountReferenceInner(Object reference) {
        return OBJECT_MAPPER.convertValue(reference, AccountReference.class);
    }
}
