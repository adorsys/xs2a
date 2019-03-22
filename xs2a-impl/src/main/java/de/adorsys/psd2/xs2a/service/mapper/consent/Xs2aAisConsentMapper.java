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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.*;
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

@Component
@RequiredArgsConstructor
public class Xs2aAisConsentMapper {
    // TODO remove this dependency. Should not be dependencies between spi-api and consent-api https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/437
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aToSpiAccountAccessMapper xs2aToSpiAccountAccessMapper;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, PsuIdData psuData, TppInfo tppInfo, int allowedFrequencyPerDay) {
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
                           ac.getTppInfo(), ac.getAisConsentRequestType(),
                           ac.getStatusChangeTimestamp()
                       )
                   )
                   .orElse(null);
    }

    public ActionStatus mapActionStatusError(MessageErrorCode error, boolean withBalance, TypeAccess access) {
        ActionStatus actionStatus = ActionStatus.FAILURE_ACCOUNT;
        if (error == MessageErrorCode.ACCESS_EXCEEDED) {
            actionStatus = ActionStatus.CONSENT_LIMIT_EXCEEDED;
        } else if (error == MessageErrorCode.CONSENT_EXPIRED) {
            actionStatus = ActionStatus.CONSENT_INVALID_STATUS;
        } else if (error == MessageErrorCode.CONSENT_UNKNOWN_400) {
            actionStatus = ActionStatus.CONSENT_NOT_FOUND;
        } else if (error == MessageErrorCode.CONSENT_INVALID) {
            if (access == TypeAccess.TRANSACTION) {
                actionStatus = ActionStatus.FAILURE_TRANSACTION;
            } else if (access == TypeAccess.BALANCE || withBalance) {
                actionStatus = ActionStatus.FAILURE_BALANCE;
            }
        }
        return actionStatus;
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
                       request.setScaAuthenticationData(data.getScaAuthenticationData());
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    private String getAuthenticationMethodId(UpdateConsentPsuDataResponse data) {
        return Optional.ofNullable(data.getChosenScaMethod())
                   .map(Xs2aAuthenticationObject::getAuthenticationMethodId)
                   .orElse(null);
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateConsentPsuDataReq request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getConsentId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    public AisAccountAccessInfo mapToAisAccountAccessInfo(Xs2aAccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(Optional.ofNullable(access.getAccounts())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setBalances(Optional.ofNullable(access.getBalances())
                                   .map(this::mapToListAccountInfo)
                                   .orElseGet(Collections::emptyList));

        accessInfo.setTransactions(Optional.ofNullable(access.getTransactions())
                                       .map(this::mapToListAccountInfo)
                                       .orElseGet(Collections::emptyList));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                            .orElse(null));

        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(accessType -> AccountAccessType.valueOf(accessType.name()))
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(List<AccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
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
                       mapToXs2aAccountAccess(ac.getAccess()),
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
                       ac.getUsageCounter()))
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsentWithNewStatus(AccountConsent consent, ConsentStatus consentStatus) {
        return Optional.ofNullable(consent)
                   .map(ac -> new AccountConsent(
                       ac.getId(),
                       ac.getAccess(),
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
                       ac.getUsageCounter()))
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
            getAccessType(ais.getAllPsd2()));
    }

    private AccountAccessType getAccessType(String type) {
        return Optional.ofNullable(type)
            .map(a -> AccountAccessType.valueOf(type))
            .orElse(null);
    }

}
