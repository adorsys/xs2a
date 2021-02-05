/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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


import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.mapper.AccountModelMapper;
import lombok.RequiredArgsConstructor;
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
    private final TppMessage2XXMapper tppMessage2XXMapper;

    public CreateConsentReq mapToCreateConsentReq(Consents consent, TppRedirectUri tppRedirectUri,
                                                  TppNotificationData tppNotificationData, String tppBrandLoggingInformation,
                                                  String instanceId) {
        return Optional.ofNullable(consent)
                   .map(cnst -> {
                       CreateConsentReq createConsentReq = new CreateConsentReq();
                       createConsentReq.setAccess(mapToAccountAccessInner(cnst.getAccess()));
                       createConsentReq.setRecurringIndicator(cnst.getRecurringIndicator());
                       createConsentReq.setValidUntil(cnst.getValidUntil());
                       createConsentReq.setFrequencyPerDay(cnst.getFrequencyPerDay());
                       createConsentReq.setCombinedServiceIndicator(BooleanUtils.toBoolean(cnst.isCombinedServiceIndicator()));
                       createConsentReq.setTppRedirectUri(tppRedirectUri);
                       createConsentReq.setTppNotificationData(tppNotificationData);
                       createConsentReq.setAvailableAccounts(mapToAccountAccessTypeFromAvailableAccounts(cnst.getAccess().getAvailableAccounts()));
                       createConsentReq.setAllPsd2(mapToAccountAccessTypeFromAllPsd2Enum(cnst.getAccess().getAllPsd2()));
                       createConsentReq.setAvailableAccountsWithBalance(mapToAccountAccessTypeFromAvailableAccountsWithBalance(cnst.getAccess().getAvailableAccountsWithBalance()));
                       createConsentReq.setTppBrandLoggingInformation(tppBrandLoggingInformation);
                       createConsentReq.setInstanceId(instanceId);
                       return createConsentReq;
                   })
                   .orElse(null);
    }

    public ConsentStatusResponse200 mapToConsentStatusResponse200(ConsentStatusResponse consentStatusResponse) {
        return Optional.ofNullable(consentStatusResponse)
                   .map(cstr -> {
                       ConsentStatusResponse200 response200 = new ConsentStatusResponse200();
                       response200.setConsentStatus(ConsentStatus.fromValue(cstr.getConsentStatus()));
                       response200.setPsuMessage(cstr.getPsuMessage());

                       return response200;
                   })
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
                                .tppMessages(tppMessage2XXMapper.mapToTppMessage2XXList(cnst.getTppMessageInformation()))
                   )
                   .orElse(null);
    }

    public ConsentInformationResponse200Json mapToConsentInformationResponse200Json(AisConsent aisConsent) {
        return Optional.ofNullable(aisConsent)
                   .map(consent ->
                            new ConsentInformationResponse200Json()
                                .access(mapToAccountAccessDomain(consent))
                                .recurringIndicator(consent.isRecurringIndicator())
                                .validUntil(consent.getValidUntil())
                                .frequencyPerDay(consent.getFrequencyPerDay())
                                .lastActionDate(consent.getLastActionDate())
                                .consentStatus(ConsentStatus.fromValue(consent.getConsentStatus().getValue()))
                   )
                   .orElse(null);
    }

    private AccountAccess mapToAccountAccessInner(de.adorsys.psd2.model.AccountAccess accountAccess) {
        return Optional.ofNullable(accountAccess)
                   .map(acs ->
                            new AccountAccess(
                                mapToXs2aAccountReferences(acs.getAccounts()),
                                mapToXs2aAccountReferences(acs.getBalances()),
                                mapToXs2aAccountReferences(acs.getTransactions()),
                                mapToAdditionalInformationAccess(acs.getAdditionalInformation())
                            ))
                   .orElse(null);
    }

    private de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess mapToAdditionalInformationAccess(AdditionalInformationAccess additionalInformationAccess) {
        return Optional.ofNullable(additionalInformationAccess)
                   .map(info -> new de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess(mapToXs2aAccountReferencesOrDefault(info.getOwnerName(), null),
                                                                                                  mapToXs2aAccountReferencesOrDefault(info.getTrustedBeneficiaries(), null)
                   ))
                   .orElse(null);
    }

    private de.adorsys.psd2.model.AccountAccess mapToAccountAccessDomain(AisConsent aisConsent) {
        AccountAccess accountAccess = aisConsent.getAccess();
        AisConsentData consentData = aisConsent.getConsentData();
        return Optional.ofNullable(accountAccess)
                   .map(access -> {
                            de.adorsys.psd2.model.AccountAccess mappedAccountAccess = new de.adorsys.psd2.model.AccountAccess();
                            mappedAccountAccess.setAccounts(accountModelMapper.mapToAccountReferences(access.getAccounts()));
                            mappedAccountAccess.setBalances(accountModelMapper.mapToAccountReferences(access.getBalances()));
                            mappedAccountAccess.setTransactions(accountModelMapper.mapToAccountReferences(access.getTransactions()));
                            mappedAccountAccess.setAvailableAccounts(
                                de.adorsys.psd2.model.AccountAccess.AvailableAccountsEnum.fromValue(
                                    Optional.ofNullable(consentData.getAvailableAccounts())
                                        .map(AccountAccessType::getDescription)
                                        .orElse(null)
                                )
                            );
                            mappedAccountAccess.setAllPsd2(
                                de.adorsys.psd2.model.AccountAccess.AllPsd2Enum.fromValue(
                                    Optional.ofNullable(consentData.getAllPsd2())
                                        .map(AccountAccessType::getDescription)
                                        .orElse(null)
                                )
                            );
                            mappedAccountAccess.setAvailableAccountsWithBalance(
                                de.adorsys.psd2.model.AccountAccess.AvailableAccountsWithBalanceEnum.fromValue(
                                    Optional.ofNullable(consentData.getAvailableAccountsWithBalance())
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
                       informationAccess.setTrustedBeneficiaries(accountModelMapper.mapToAccountReferences(info.getTrustedBeneficiaries()));
                       return informationAccess;
                   })
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccounts(de.adorsys.psd2.model.AccountAccess.AvailableAccountsEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAllPsd2Enum(de.adorsys.psd2.model.AccountAccess.AllPsd2Enum allPsd2Enum) {
        return Optional.ofNullable(allPsd2Enum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessTypeFromAvailableAccountsWithBalance(de.adorsys.psd2.model.AccountAccess.AvailableAccountsWithBalanceEnum accountsEnum) {
        return Optional.ofNullable(accountsEnum)
                   .flatMap(en -> AccountAccessType.getByDescription(en.toString()))
                   .orElse(null);
    }

    public List<AccountReference> mapToXs2aAccountReferences(List<de.adorsys.psd2.model.AccountReference> references) {
        return mapToXs2aAccountReferencesOrDefault(references, Collections.emptyList());
    }

    private List<AccountReference> mapToXs2aAccountReferencesOrDefault(List<de.adorsys.psd2.model.AccountReference> references, List<AccountReference> defaultValue) {
        return Optional.ofNullable(references)
                   .map(ref -> ref.stream()
                                   .map(this::mapToAccountReference)
                                   .collect(Collectors.toList()))
                   .orElse(defaultValue);
    }

    public AccountReference mapToAccountReference(Object reference) {
        return xs2aObjectMapper.convertValue(reference, AccountReference.class);
    }

    public UpdateConsentPsuDataReq mapToUpdatePsuData(PsuIdData psuData, String consentId, String authorizationId, Map body) {
        UpdateConsentPsuDataReq updatePsuData = new UpdateConsentPsuDataReq();
        updatePsuData.setPsuData(psuData);
        updatePsuData.setConsentId(consentId);
        updatePsuData.setAuthorizationId(authorizationId);
        updatePsuData.setAuthorisationType(AuthorisationType.CONSENT);
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

    public Authorisations mapToAuthorisations(Xs2aPaymentCancellationAuthorisationSubResource idsContainer) {
        return Optional.ofNullable(idsContainer.getAuthorisationIds())
                   .map(this::buildAuthorisations)
                   .orElseGet(Authorisations::new);
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

    private Authorisations buildAuthorisations(List<String> authorisationIds) {
        Authorisations authorisations = new Authorisations();
        AuthorisationsList authorisationsList = new AuthorisationsList();
        authorisationsList.addAll(authorisationIds);
        authorisations.setAuthorisationIds(authorisationsList);
        return authorisations;
    }
}
