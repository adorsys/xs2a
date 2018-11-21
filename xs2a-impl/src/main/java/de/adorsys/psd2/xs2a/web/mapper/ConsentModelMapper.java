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

package de.adorsys.psd2.xs2a.web.mapper;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsentModelMapper {
    private final CoreObjectsMapper coreObjectsMapper;
    private final ObjectMapper objectMapper;

    public CreateConsentReq mapToCreateConsentReq(Consents consent) {
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

    public ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
                   .map(cstr -> new ConsentStatusResponse200().consentStatus(ConsentStatus.fromValue(cstr.getConsentStatus())))
                   .orElse(null);
    }

    public StartScaprocessResponse mapToStartScaProcessResponse(Xsa2CreatePisConsentAuthorisationResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> new StartScaprocessResponse()
                                 .scaStatus(coreObjectsMapper.mapToModelScaStatus(r.getScaStatus()))
                                 ._links(objectMapper.convertValue(r.getLinks(), Map.class)))
                   .orElse(null);
    }

    public StartScaprocessResponse mapToStartScaProcessResponse(Xs2aCreatePisConsentCancellationAuthorisationResponse response) {
        return new StartScaprocessResponse()
                   .scaStatus(coreObjectsMapper.mapToModelScaStatus(response.getScaStatus()))
                   ._links(objectMapper.convertValue(response.getLinks(), Map.class));
    }

    public UpdatePsuAuthenticationResponse mapToUpdatePsuAuthenticationResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r ->
                            new UpdatePsuAuthenticationResponse()
                                ._links(objectMapper.convertValue(response.getLinks(), Map.class))
                                .scaMethods(getAvailableScaMethods(r.getAvailableScaMethods()))
                                .scaStatus(
                                    Optional.ofNullable(r.getScaStatus())
                                        .map(s -> ScaStatus.valueOf(s.name()))
                                        .orElse(null)
                                )
                                .challengeData(coreObjectsMapper.mapToChallengeData(response.getChallengeData()))
                                .chosenScaMethod(mapToChosenScaMethod(response.getChosenScaMethod()))
                   )
                   .orElse(null);
    }

    public ConsentsResponse201 mapToConsentsResponse201(CreateConsentResponse createConsentResponse) {
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

    public ConsentInformationResponse200Json mapToConsentInformationResponse200Json(AccountConsent accountConsent) {
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
                                                  .map(arr -> Arrays.stream(arr)
                                                                  .map(au -> new AuthenticationObject()
                                                                                 .authenticationType(AuthenticationType.fromValue(au.getAuthenticationType()))
                                                                                 .authenticationVersion(au.getAuthenticationVersion())
                                                                                 .authenticationMethodId(au.getAuthenticationMethodId())
                                                                                 .name(au.getName())
                                                                                 .explanation(au.getExplanation()))
                                                                  .collect(Collectors.toList()))
                                                  .orElseGet(Collections::emptyList);
        ScaMethods scaMethods = new ScaMethods();
        scaMethods.addAll(authList);

        return scaMethods;
    }

    private Xs2aAccountAccess mapToAccountAccessInner(AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
                   .map(acs ->
                            new Xs2aAccountAccess(
                                mapToXs2aAccountReferences(acs.getAccounts()),
                                mapToXs2aAccountReferences(acs.getBalances()),
                                mapToXs2aAccountReferences(acs.getTransactions()),
                                mapToAccountAccessTypeFromAvailableAccounts(acs.getAvailableAccounts()),
                                mapToAccountAccessTypeFromAllPsd2Enum(acs.getAllPsd2())
                            ))
                   .orElse(null);
    }

    private AccountAccess mapToAccountAccessDomain(Xs2aAccountAccess accountAccess) {
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

    private Xs2aAccountAccessType mapToAccountAccessTypeFromAvailableAccounts(AccountAccess.AvailableAccountsEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> Xs2aAccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private Xs2aAccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
                   .flatMap(en -> Xs2aAccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private List<Xs2aAccountReference> mapToXs2aAccountReferences(List<Object> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToXs2aAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private Xs2aAccountReference mapToXs2aAccountReference(Object reference) {
        return objectMapper.convertValue(reference, Xs2aAccountReference.class);
    }

    public UpdateConsentPsuDataReq mapToUpdatePsuData(PsuIdData psuData, String consentId, String authorizationId, Map body) {
        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
        updatePsuData.setPsuData(psuData);
        updatePsuData.setConsentId(consentId);
        updatePsuData.setAuthorizationId(authorizationId);

        if (!body.isEmpty()) {
            Optional.ofNullable(body.get("psuData"))
                .map(o -> (LinkedHashMap<String, String>) o)
                .ifPresent(psuDataMap -> updatePsuData.setPassword(psuDataMap.get("password")));

            Optional.ofNullable(body.get("authenticationMethodId"))
                .map(o -> (String) o)
                .ifPresent(updatePsuData::setAuthenticationMethodId);

            Optional.ofNullable(body.get("scaAuthenticationData"))
                .map(o -> (String) o)
                .ifPresent(updatePsuData::setScaAuthenticationData);
        } else {
            updatePsuData.setUpdatePsuIdentification(true);
        }

        return updatePsuData;
    }

    public Xs2aUpdatePisConsentPsuDataRequest mapToPisUpdatePsuData(PsuIdData psuData, String paymentId, String authorisationId, String paymentService, Map body) {
        Xs2aUpdatePisConsentPsuDataRequest request = new Xs2aUpdatePisConsentPsuDataRequest();
        request.setPsuData(psuData);
        request.setPaymentId(paymentId);
        request.setAuthorizationId(authorisationId);
        request.setPaymentService(paymentService);
        if (!body.isEmpty()) {
            Optional.ofNullable(body.get("psuData"))
                .map(o -> (LinkedHashMap<String, String>) o)
                .ifPresent(psuDataMap -> request.setPassword(psuDataMap.get("password")));

            Optional.ofNullable(body.get("authenticationMethodId"))
                .map(o -> (String) o)
                .ifPresent(request::setAuthenticationMethodId);

            Optional.ofNullable(body.get("scaAuthenticationData"))
                .ifPresent(authData -> request.setScaAuthenticationData((String) authData));
        }
        return request;
    }

    public UpdatePsuAuthenticationResponse mapToUpdatePsuAuthenticationResponse(Xs2aUpdatePisConsentPsuDataResponse response) {
        return new UpdatePsuAuthenticationResponse()
                   ._links(objectMapper.convertValue(response.getLinks(), Map.class))
                   .scaMethods(getAvailableScaMethods(response.getAvailableScaMethods()))
                   .chosenScaMethod(mapToChosenScaMethod(response.getChosenScaMethod()))
                   .challengeData(coreObjectsMapper.mapToChallengeData(response.getChallengeData()))
                   .scaStatus(Optional.ofNullable(response.getScaStatus())
                       .map(s -> ScaStatus.fromValue(s.getValue()))
                       .orElse(ScaStatus.FAILED));
    }

    private ScaMethods getAvailableScaMethods(List<Xs2aAuthenticationObject> availableScaMethods) {
        ScaMethods scaMethods = new ScaMethods();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            availableScaMethods.forEach(a -> scaMethods.add(new AuthenticationObject()
                                                                .authenticationMethodId(a.getAuthenticationMethodId())
                                                                .authenticationType(AuthenticationType.fromValue(a.getAuthenticationType()))
                                                                .authenticationVersion(a.getAuthenticationVersion())
                                                                .name(a.getName())
                                                                .explanation(a.getExplanation())));
        }
        return scaMethods;
    }

    private ChosenScaMethod mapToChosenScaMethod(Xs2aAuthenticationObject xs2aAuthenticationObject) {
        return Optional.ofNullable(xs2aAuthenticationObject)
                   .map(ch -> {
                       ChosenScaMethod method = new ChosenScaMethod();
                       method.setAuthenticationMethodId(ch.getAuthenticationMethodId());
                       method.setAuthenticationType(AuthenticationType.fromValue(ch.getAuthenticationType()));
                       method.setAuthenticationVersion(ch.getAuthenticationVersion());
                       method.setName(ch.getName());
                       method.setExplanation(ch.getExplanation());
                       return method;
                   }).orElse(null);
    }
}
