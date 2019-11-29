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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    // TODO remove this dependency. Should not be dependencies between spi-api and consent-api https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/437
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aToSpiAccountAccessMapper xs2aToSpiAccountAccessMapper;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, PsuIdData psuData, TppInfo tppInfo, int allowedFrequencyPerDay, String internalRequestId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuData(psuData);
                       aisRequest.setTppInfo(tppInfo);
                       aisRequest.setRequestedFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAllowedFrequencyPerDay(allowedFrequencyPerDay);
                       aisRequest.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());
                       aisRequest.setTppRedirectUri(r.getTppRedirectUri());
                       aisRequest.setInternalRequestId(internalRequestId);
                       aisRequest.setTppNotificationUri(req.getTppNotificationUri());
                       aisRequest.setNotificationSupportedModes(req.getNotificationSupportedModes());
                       return aisRequest;
                   })
                   .orElse(null);
    }

    public SpiAccountConsent mapToSpiAccountConsent(AccountConsent accountConsent) {
        return Optional.ofNullable(accountConsent)
                   .map(ac -> new SpiAccountConsent(
                            ac.getId(),
                            xs2aToSpiAccountAccessMapper.mapToAccountAccess(ac.getAccess()),
                            ac.isRecurringIndicator(),
                            ac.getValidUntil(),
                            ac.getFrequencyPerDay(),
                            ac.getLastActionDate(),
                            ac.getConsentStatus(),
                            ac.isWithBalance(),
                            ac.isTppRedirectPreferred(),
                            xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(ac.getPsuIdDataList()),
                            ac.getTppInfo(),
                            ac.getAisConsentRequestType(),
                            ac.getStatusChangeTimestamp(),
                            ac.getCreationTimestamp()
                        )
                   )
                   .orElse(null);
    }

    public UpdateConsentPsuDataReq mapToSpiUpdateConsentPsuDataReq(UpdateConsentPsuDataResponse updatePsuDataResponse,
                                                                   UpdateConsentPsuDataReq updatePsuDataRequest) {
        return Optional.ofNullable(updatePsuDataResponse)
                   .map(data -> {
                       PsuIdData psuIdDataFromRequest = updatePsuDataRequest.getPsuData();
                       UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
                       request.setPsuData(new PsuIdData(psuIdDataFromRequest.getPsuId(), psuIdDataFromRequest.getPsuIdType(), psuIdDataFromRequest.getPsuCorporateId(), psuIdDataFromRequest.getPsuCorporateIdType()));
                       request.setConsentId(updatePsuDataRequest.getConsentId());
                       request.setAuthorizationId(updatePsuDataRequest.getAuthorizationId());
                       request.setAuthenticationMethodId(getAuthenticationMethodId(data));
                       request.setScaAuthenticationData(updatePsuDataRequest.getScaAuthenticationData());
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    public UpdateConsentPsuDataReq mapToSpiUpdateConsentPsuDataReq(UpdateAuthorisationRequest request,
                                                                   AuthorisationProcessorResponse response) {
        return Optional.ofNullable(response)
                   .map(data -> {
                       PsuIdData psuIdDataFromRequest = request.getPsuData();
                       UpdateConsentPsuDataReq req = new UpdateConsentPsuDataReq();
                       req.setPsuData(new PsuIdData(psuIdDataFromRequest.getPsuId(), psuIdDataFromRequest.getPsuIdType(), psuIdDataFromRequest.getPsuCorporateId(), psuIdDataFromRequest.getPsuCorporateIdType()));
                       req.setConsentId(request.getBusinessObjectId());
                       req.setAuthorizationId(request.getAuthorisationId());
                       req.setAuthenticationMethodId(Optional.ofNullable(data.getChosenScaMethod())
                                                         .map(Xs2aAuthenticationObject::getAuthenticationMethodId)
                                                         .orElse(null));
                       req.setScaAuthenticationData(request.getScaAuthenticationData());
                       req.setScaStatus(data.getScaStatus());
                       return req;
                   })
                   .orElse(null);
    }

    private String getAuthenticationMethodId(UpdateConsentPsuDataResponse data) {
        return Optional.ofNullable(data.getChosenScaMethod())
                   .map(Xs2aAuthenticationObject::getAuthenticationMethodId)
                   .orElse(null);
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getBusinessObjectId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    public AisAccountAccessInfo mapToAisAccountAccessInfo(Xs2aAccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(mapToListAccountInfo(access.getAccounts()));
        accessInfo.setBalances(mapToListAccountInfo(access.getBalances()));
        accessInfo.setTransactions(mapToListAccountInfo(access.getTransactions()));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                            .orElse(null));

        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                  .orElse(null));

        accessInfo.setAvailableAccountsWithBalance(Optional.ofNullable(access.getAvailableAccountsWithBalance())
                                                       .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                                       .orElse(null));

        accessInfo.setAccountAdditionalInformationAccess(Optional.ofNullable(access.getAdditionalInformationAccess())
                                                             .map(this::mapToAccountAdditionalInformationAccess)
                                                             .orElse(null));

        return accessInfo;
    }

    private AccountAdditionalInformationAccess mapToAccountAdditionalInformationAccess(AdditionalInformationAccess info) {
        return new AccountAdditionalInformationAccess(
            mapToListAccountInfoOrDefault(info.getOwnerName(), null),
            mapToListAccountInfoOrDefault(info.getOwnerAddress(), null));
    }

    private List<AccountInfo> mapToListAccountInfo(List<AccountReference> refs) {
        return emptyIfNull(refs).stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private List<AccountInfo> mapToListAccountInfoOrDefault(List<AccountReference> refs, List<AccountInfo> defaultValue) {
        return Optional.ofNullable(refs)
                   .map(this::mapToListAccountInfo)
                   .orElse(defaultValue);
    }

    private AccountInfo mapToAccountInfo(AccountReference ref) {
        AccountReferenceSelector selector = ref.getUsedAccountReferenceSelector();
        return AccountInfo.builder()
                   .resourceId(ref.getResourceId())
                   .accountIdentifier(selector.getAccountValue())
                   .currency(Optional.ofNullable(ref.getCurrency())
                                 .map(Currency::getCurrencyCode)
                                 .orElse(null))
                   .accountReferenceType(selector.getAccountReferenceType())
                   .aspspAccountId(ref.getAspspAccountId())
                   .build();
    }

    public AccountConsent mapToAccountConsent(AisAccountConsent ais) {
        return Optional.ofNullable(ais)
                   .map(ac -> new AccountConsent(
                       ac.getId(),
                       mapToXs2aAccountAccess(ac.getTppAccess()),
                       mapToXs2aAccountAccess(ac.getAspspAccess()),
                       ac.isRecurringIndicator(),
                       ac.getValidUntil(),
                       ac.getFrequencyPerDay(),
                       ac.getLastActionDate(),
                       ac.getConsentStatus(),
                       ac.isWithBalance(),
                       ac.isTppRedirectPreferred(),
                       ac.getPsuIdDataList(),
                       ac.getTppInfo(),
                       ac.getAisConsentRequestType(),
                       ac.isMultilevelScaRequired(),
                       mapToAccountConsentAuthorisation(ais.getAccountConsentAuthorizations()),
                       ac.getStatusChangeTimestamp(),
                       ac.getUsageCounterMap(),
                       ac.getCreationTimestamp()))
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsentWithNewStatus(AccountConsent consent, ConsentStatus consentStatus) {
        return Optional.ofNullable(consent)
                   .map(ac -> new AccountConsent(
                       ac.getId(),
                       ac.getAccess(),
                       ac.getAspspAccess(),
                       ac.isRecurringIndicator(),
                       ac.getValidUntil(),
                       ac.getFrequencyPerDay(),
                       ac.getLastActionDate(),
                       consentStatus,
                       ac.isWithBalance(),
                       ac.isTppRedirectPreferred(),
                       ac.getPsuIdDataList(),
                       ac.getTppInfo(),
                       ac.getAisConsentRequestType(),
                       ac.isMultilevelScaRequired(),
                       ac.getAuthorisations(),
                       ac.getStatusChangeTimestamp(),
                       ac.getUsageCounterMap(),
                       ac.getCreationTimestamp()))
                   .orElse(null);
    }

    private List<AccountConsentAuthorization> mapToAccountConsentAuthorisation(List<AisAccountConsentAuthorisation> accountConsentAuthorizations) {
        if (CollectionUtils.isEmpty(accountConsentAuthorizations)) {
            return Collections.emptyList();
        }
        return accountConsentAuthorizations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AccountConsentAuthorization mapToAccountConsentAuthorisation(AisAccountConsentAuthorisation aisAccountConsentAuthorisation) {
        return Optional.ofNullable(aisAccountConsentAuthorisation)
                   .map(auth -> {
                       AccountConsentAuthorization accountConsentAuthorisation = new AccountConsentAuthorization();
                       accountConsentAuthorisation.setId(auth.getId());
                       accountConsentAuthorisation.setPsuIdData(auth.getPsuIdData());
                       accountConsentAuthorisation.setScaStatus(auth.getScaStatus());
                       return accountConsentAuthorisation;
                   })
                   .orElse(null);
    }

    private Xs2aAccountAccess mapToXs2aAccountAccess(AisAccountAccess ais) {
        return new Xs2aAccountAccess(
            ais.getAccounts(),
            ais.getBalances(),
            ais.getTransactions(),
            getAccessType(ais.getAvailableAccounts()),
            getAccessType(ais.getAllPsd2()),
            getAccessType(ais.getAvailableAccountsWithBalance()),
            mapToAdditionalInformationAccess(ais.getAccountAdditionalInformationAccess()));
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(AdditionalInformationAccess accountAdditionalInformationAccess) {
        return  Optional.ofNullable(accountAdditionalInformationAccess)
                       .map(info -> new AdditionalInformationAccess(info.getOwnerName(), info.getOwnerAddress()))
                       .orElse(null);
    }

    private AccountAccessType getAccessType(String type) {
        return Optional.ofNullable(type)
                   .map(a -> AccountAccessType.valueOf(type))
                   .orElse(null);
    }

}
