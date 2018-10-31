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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiAccountAccessMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.consent.api.AccountInfo;
import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessType;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import lombok.RequiredArgsConstructor;
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
    private final SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    private final SpiToXs2aPsuDataMapper spiToXs2aPsuDataMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aToSpiAccountAccessMapper xs2aToSpiAccountAccessMapper;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, PsuIdData psuData, String tppId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuData(psuData);
                       aisRequest.setTppId(tppId);
                       aisRequest.setFrequencyPerDay(r.getFrequencyPerDay());
                       aisRequest.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       aisRequest.setValidUntil(r.getValidUntil());
                       aisRequest.setRecurringIndicator(r.isRecurringIndicator());
                       aisRequest.setCombinedServiceIndicator(r.isCombinedServiceIndicator());

                       return aisRequest;
                   })
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
                   .map(ac -> new AccountConsent(
                           ac.getId(),
                           spiToXs2aAccountAccessMapper.mapToAccountAccess(ac.getAccess()),
                           ac.isRecurringIndicator(),
                           ac.getValidUntil(),
                           ac.getFrequencyPerDay(),
                           ac.getLastActionDate(),
                           ac.getConsentStatus(),
                           ac.isWithBalance(),
                           ac.isTppRedirectPreferred(),
                           spiToXs2aPsuDataMapper.mapToPsuIdData(ac.getPsuData()),
                           ac.getTppId()
                       )
                   )
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
                           xs2aToSpiPsuDataMapper.mapToSpiPsuData(ac.getPsuData()),
                           ac.getTppId()
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
                       UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
                       request.setPsuData(new PsuIdData(data.getPsuId(), null, null, null));
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

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateConsentPsuDataReq request) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getConsentId());
        accountConfirmation.setPsuId(Optional.ofNullable(request.getPsuData()).map(PsuIdData::getPsuId).orElse(null));
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
                                            .map(accessType -> AisAccountAccessType.valueOf(accessType.name()))
                                            .orElse(null));
        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(accessType -> AisAccountAccessType.valueOf(accessType.name()))
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(List<Xs2aAccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToAccountInfo(Xs2aAccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setResourceId(ref.getResourceId());
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }
}
