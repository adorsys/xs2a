/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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


import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
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
    private final Xs2aObjectMapper xs2aObjectMapper;
    public final AccountModelMapper accountModelMapper;
    private final HrefLinkMapper hrefLinkMapper;
    private final ScaMethodsMapper scaMethodsMapper;

    public CreateConsentReq mapToCreateConsentReq(Consents consent, TppRedirectUri tppRedirectUri, TppNotificationData tppNotificationData) {
        return Optional.ofNullable(consent)
                   .map(cnst -> {
                       CreateConsentReq createAisConsentRequest = new CreateConsentReq();
                       createAisConsentRequest.setAccess(mapToAccountAccessInner(cnst.getAccess()));
                       createAisConsentRequest.setRecurringIndicator(cnst.getRecurringIndicator());
                       createAisConsentRequest.setValidUntil(cnst.getValidUntil());
                       createAisConsentRequest.setFrequencyPerDay(cnst.getFrequencyPerDay());
                       createAisConsentRequest.setCombinedServiceIndicator(BooleanUtils.toBoolean(cnst.isCombinedServiceIndicator()));
                       createAisConsentRequest.setTppRedirectUri(tppRedirectUri);
                       createAisConsentRequest.setTppNotificationData(tppNotificationData);
                       return createAisConsentRequest;
                   })
                   .orElse(null);
    }

    public ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
                   .map(cstr -> new ConsentStatusResponse200().consentStatus(ConsentStatus.fromValue(cstr.getConsentStatus())))
                   .orElse(null);
    }

    public ConsentsResponse201 mapToConsentsResponse201(CreateConsentResponse createConsentResponse) {
        return Optional.ofNullable(createConsentResponse)
                   .map(cnst ->
                            new ConsentsResponse201()
                                .consentStatus(ConsentStatus.fromValue(cnst.getConsentStatus()))
                                .consentId(cnst.getConsentId())
                                .scaMethods(scaMethodsMapper.mapToScaMethods(cnst.getScaMethods()))
                                ._links(hrefLinkMapper.mapToLinksMap(cnst.getLinks()))
                                .psuMessage(cnst.getPsuMessage())
                                .tppMessages(mapToTppMessage2XXList(cnst.getTppMessageInformation()))
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

    private Xs2aAccountAccess mapToAccountAccessInner(AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
                   .map(acs ->
                            new Xs2aAccountAccess(
                                mapToXs2aAccountReferences(acs.getAccounts()),
                                mapToXs2aAccountReferences(acs.getBalances()),
                                mapToXs2aAccountReferences(acs.getTransactions()),
                                mapToAccountAccessTypeFromAvailableAccounts(acs.getAvailableAccounts()),
                                mapToAccountAccessTypeFromAllPsd2Enum(acs.getAllPsd2()),
                                mapToAccountAccessTypeFromAvailableAccountsWithBalance(acs.getAvailableAccountsWithBalance()),
                                mapToAdditionalInformationAccess(acs.getAdditionalInformation())
                            ))
                   .orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess mapToAdditionalInformationAccess(AdditionalInformationAccess additionalInformationAccess) {
        return Optional.ofNullable(additionalInformationAccess)
                   .map(info -> new de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess(mapToXs2aAccountReferencesOrDefault(info.getOwnerName(), null)))
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
                            mappedAccountAccess.setAvailableAccountsWithBalance(
                                AccountAccess.AvailableAccountsWithBalanceEnum.fromValue(
                                    Optional.ofNullable(access.getAvailableAccountsWithBalance())
                                        .map(AccountAccessType::getDescription)
                                        .orElse(null)
                                )
                            );

                            mappedAccountAccess.setAdditionalInformation(mapToAdditionalInformationAccess(access.getAdditionalInformationAccess()));

                            return mappedAccountAccess;
                        }
                   )
                   .orElse(null);
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess additionalInformationAccess) {
        return Optional.ofNullable(additionalInformationAccess)
                   .map(info -> {
                       if (info.noAdditionalInformationAccess()) {
                           return null;
                       }
                       AdditionalInformationAccess informationAccess = new AdditionalInformationAccess();
                       informationAccess.setOwnerName(accountModelMapper.mapToAccountReferences(info.getOwnerName()));
                       return informationAccess;
                   })
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

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccountsWithBalance(AccountAccess.AvailableAccountsWithBalanceEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private List<AccountReference> mapToXs2aAccountReferences(List<de.adorsys.psd2.model.AccountReference> references) {
        return mapToXs2aAccountReferencesOrDefault(references, Collections.emptyList());
    }

    private List<AccountReference> mapToXs2aAccountReferencesOrDefault(List<de.adorsys.psd2.model.AccountReference> references, List<AccountReference> defaultValue) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElse(defaultValue);
    }

    private AccountReference mapToAccountReference(Object reference) {
        return xs2aObjectMapper.convertValue(reference, AccountReference.class);
    }

    public UpdateConsentPsuDataReq mapToUpdatePsuData(PsuIdData psuData, String consentId, String authorizationId, Map body) {
        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
        updatePsuData.setPsuData(psuData);
        updatePsuData.setConsentId(consentId);
        updatePsuData.setAuthorizationId(authorizationId);
        if (body != null && !body.isEmpty()) {
            Optional.ofNullable(body.get("psuData"))
                .map(o -> (LinkedHashMap<String, String>) o)
                .ifPresent(psuDataMap -> updatePsuData.setPassword(psuDataMap.get("password")));

            Optional.ofNullable(body.get("authenticationMethodId"))
                .map(o -> (String) o)
                .ifPresent(updatePsuData::setAuthenticationMethodId);

            Optional.ofNullable(body.get("scaAuthenticationData"))
                .map(o -> (String) o)
                .ifPresent(updatePsuData::setScaAuthenticationData);

            Optional.ofNullable(body.get("confirmationCode"))
                .map(o -> (String) o)
                .ifPresent(updatePsuData::setConfirmationCode);

        } else {
            updatePsuData.setUpdatePsuIdentification(true);
        }

        return updatePsuData;
    }

    public Cancellations mapToCancellations(Xs2aPaymentCancellationAuthorisationSubResource idsContainer) {
        return Optional.ofNullable(idsContainer.getCancellationIds())
                   .map(this::buildCancellations)
                   .orElseGet(Cancellations::new);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataRequest mapToPisUpdatePsuData(PsuIdData psuData, String paymentId, String authorisationId, PaymentType paymentService, String paymentProduct, Map body) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        request.setPsuData(psuData);
        request.setPaymentId(paymentId);
        request.setAuthorisationId(authorisationId);
        request.setPaymentService(paymentService);
        request.setPaymentProduct(paymentProduct);
        if (body != null && !body.isEmpty()) {
            Optional.ofNullable(body.get("psuData"))
                .map(o -> (LinkedHashMap<String, String>) o)
                .ifPresent(psuDataMap -> request.setPassword(psuDataMap.get("password")));

            Optional.ofNullable(body.get("authenticationMethodId"))
                .map(o -> (String) o)
                .ifPresent(request::setAuthenticationMethodId);

            Optional.ofNullable(body.get("scaAuthenticationData"))
                .ifPresent(authData -> request.setScaAuthenticationData((String) authData));

            Optional.ofNullable(body.get("confirmationCode"))
                .ifPresent(code -> request.setConfirmationCode((String) code));
        } else {
            request.setUpdatePsuIdentification(true);
        }
        return request;
    }

    private Cancellations buildCancellations(List<String> cancellationIds) {
        Cancellations cancellations = new Cancellations();
        CancellationList cancellationList = new CancellationList();
        cancellationList.addAll(cancellationIds);
        cancellations.setCancellationIds(cancellationList);
        return cancellations;
    }


    private List<TppMessage2XX> mapToTppMessage2XXList(Set<TppMessageInformation> tppMessages) {
        if (CollectionUtils.isEmpty(tppMessages)) {
            return null;
        }
        return tppMessages.stream()
                   .map(this::mapToTppMessage2XX)
                   .collect(Collectors.toList());
    }

    private TppMessage2XX mapToTppMessage2XX(TppMessageInformation tppMessage) {
        TppMessage2XX tppMessage2XX = new TppMessage2XX();
        tppMessage2XX.setCategory(TppMessageCategory.fromValue(tppMessage.getCategory().name()));
        tppMessage2XX.setCode(MessageCode2XX.WARNING);
        tppMessage2XX.setPath(tppMessage.getPath());
        tppMessage2XX.setText(tppMessage.getText());

        return tppMessage2XX;
    }
}
