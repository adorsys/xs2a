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
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
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
    public final AccountModelMapper accountModelMapper;
    private final HrefLinkMapper hrefLinkMapper;

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

    public StartScaprocessResponse mapToStartScaProcessResponse(Xs2aCreatePisAuthorisationResponse response) {
        return Optional.ofNullable(response)
                   .map(r -> new StartScaprocessResponse()
                                 .scaStatus(coreObjectsMapper.mapToModelScaStatus(r.getScaStatus()))
                                 .authorisationId(r.getAuthorisationId())
                                 ._links(hrefLinkMapper.mapToLinksMap(r.getLinks())))
                   .orElse(null);
    }

    public StartScaprocessResponse mapToStartScaProcessResponse(Xs2aCreatePisCancellationAuthorisationResponse response) {
        return new StartScaprocessResponse()
                   .scaStatus(coreObjectsMapper.mapToModelScaStatus(response.getScaStatus()))
                   ._links(hrefLinkMapper.mapToLinksMap(response.getLinks()));
    }

    public UpdatePsuAuthenticationResponse mapToUpdatePsuAuthenticationResponse(UpdateConsentPsuDataResponse response) {
        return Optional.ofNullable(response)
                   .map(r ->
                            new UpdatePsuAuthenticationResponse()
                                ._links(hrefLinkMapper.mapToLinksMap(response.getLinks()))
                                .scaMethods(getAvailableScaMethods(r.getAvailableScaMethods()))
                                .scaStatus(
                                    Optional.ofNullable(r.getScaStatus())
                                        .map(s -> ScaStatus.valueOf(s.name()))
                                        .orElse(null)
                                )
                                .challengeData(coreObjectsMapper.mapToChallengeData(response.getChallengeData()))
                                .chosenScaMethod(mapToChosenScaMethod(response.getChosenScaMethodForPsd2Response()))
                                .psuMessage(r.getPsuMessage())
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
                                ._links(hrefLinkMapper.mapToLinksMap(cnst.getLinks()))
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

    public Authorisations mapToAuthorisations(Xs2aAuthorisationSubResources xs2AAuthorisationSubResources) {
        Authorisations authorisations = new Authorisations();
        AuthorisationsList authorisationsList = new AuthorisationsList();
        authorisationsList.addAll(xs2AAuthorisationSubResources.getAuthorisationIds());
        authorisations.setAuthorisationIds(authorisationsList);
        return authorisations;
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
                            mappedAccountAccess.setAccounts(accountModelMapper.mapToAccountReferences(access.getAccounts()));
                            mappedAccountAccess.setBalances(accountModelMapper.mapToAccountReferences(access.getBalances()));
                            mappedAccountAccess.setTransactions(accountModelMapper.mapToAccountReferences(access.getTransactions()));
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
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private List<AccountReference> mapToXs2aAccountReferences(List<de.adorsys.psd2.model.AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElseGet(Collections::emptyList);
    }

    private AccountReference mapToAccountReference(Object reference) {
        return objectMapper.convertValue(reference, AccountReference.class);
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

    public CancellationList mapToCancellationList(Xs2aPaymentCancellationAuthorisationSubResource idsContainer) {
        CancellationList list = new CancellationList();

        list.addAll(Optional.ofNullable(idsContainer.getCancellationIds())
                        .map(ArrayList::new)
                        .orElseGet(ArrayList::new));
        return list;
    }

    public Xs2aUpdatePisCommonPaymentPsuDataRequest mapToPisUpdatePsuData(PsuIdData psuData, String paymentId, String authorisationId, String paymentService, String paymentProduct, Map body) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPsuData(psuData);
        request.setPaymentId(paymentId);
        request.setAuthorisationId(authorisationId);
        request.setPaymentService(paymentService);
        request.setPaymentProduct(paymentProduct);
        if (!body.isEmpty()) {
            Optional.ofNullable(body.get("psuData"))
                .map(o -> (LinkedHashMap<String, String>) o)
                .ifPresent(psuDataMap -> request.setPassword(psuDataMap.get("password")));

            Optional.ofNullable(body.get("authenticationMethodId"))
                .map(o -> (String) o)
                .ifPresent(request::setAuthenticationMethodId);

            Optional.ofNullable(body.get("scaAuthenticationData"))
                .ifPresent(authData -> request.setScaAuthenticationData((String) authData));
        } else {
            request.setUpdatePsuIdentification(true);
        }
        return request;
    }

    public UpdatePsuAuthenticationResponse mapToUpdatePsuAuthenticationResponse(Xs2aUpdatePisCommonPaymentPsuDataResponse response) {
        return new UpdatePsuAuthenticationResponse()
                   ._links(hrefLinkMapper.mapToLinksMap(response.getLinks()))
                   .scaMethods(getAvailableScaMethods(response.getAvailableScaMethods()))
                   .chosenScaMethod(mapToChosenScaMethod(response.getChosenScaMethodForPsd2Response()))
                   .challengeData(coreObjectsMapper.mapToChallengeData(response.getChallengeData()))
                   .psuMessage(response.getPsuMessage())
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
