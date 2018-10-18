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
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiXs2aAccountMapper;
import de.adorsys.psd2.consent.api.*;
import de.adorsys.psd2.consent.api.ais.AccountAccessType;
import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
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
    private final SpiXs2aAccountMapper spiXs2aAccountMapper;

    public CreateAisConsentRequest mapToCreateAisConsentRequest(CreateConsentReq req, String psuId, String tppId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       CreateAisConsentRequest aisRequest = new CreateAisConsentRequest();
                       aisRequest.setPsuId(psuId);
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
                       mapToAccountAccess(ac.getAccess()),
                       ac.isRecurringIndicator(),
                       ac.getValidUntil(),
                       ac.getFrequencyPerDay(),
                       ac.getLastActionDate(),
                       ConsentStatus.valueOf(ac.getConsentStatus().name()),
                       ac.isWithBalance(),
                       ac.isTppRedirectPreferred()))
                   .orElse(null);
    }

    public Optional<ConsentStatus> mapToConsentStatus(SpiConsentStatus spiConsentStatus) {
        return Optional.ofNullable(spiConsentStatus)
                   .map(status -> ConsentStatus.valueOf(status.name()));
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
                       request.setPsuId(updatePsuDataResponse.getPsuId());
                       request.setConsentId(updatePsuDataRequest.getConsentId());
                       request.setAuthorizationId(updatePsuDataRequest.getAuthorizationId());
                       request.setAuthenticationMethodId(updatePsuDataResponse.getAuthenticationMethodId());
                       request.setAuthenticationMethodId(updatePsuDataResponse.getChosenScaMethod());
                       request.setScaAuthenticationData(updatePsuDataResponse.getScaAuthenticationData());
                       request.setScaStatus(data.getScaStatus());
                       return request;
                   })
                   .orElse(null);
    }

    public Optional<SpiConsentStatus> mapToSpiConsentStatus(CmsConsentStatus consentStatus) {
        return Optional.ofNullable(consentStatus)
                   .map(status -> SpiConsentStatus.valueOf(status.name()));
    }

    public List<CmsScaMethod> mapToCmsScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.stream()
                   .map(this::mapToCmsScaMethod)
                   .collect(Collectors.toList());
    }

    /**
     * @deprecated since 1.8. Will be removed in 1.10
     * {@link #mapToSpiScaConfirmation(UpdateConsentPsuDataReq)} should be used instead
     */
    @Deprecated
    public SpiAccountConfirmation mapToSpiAccountConfirmation(UpdateConsentPsuDataReq request) {
        return Optional.ofNullable(request)
                   .map(r -> {
                       SpiAccountConfirmation accountConfirmation = new SpiAccountConfirmation();
                       accountConfirmation.setConsentId(r.getConsentId());
                       accountConfirmation.setPsuId(r.getPsuId());
                       accountConfirmation.setTanNumber(r.getScaAuthenticationData());
                       return accountConfirmation;
                   })
                   .orElse(null);
    }

    public SpiScaConfirmation mapToSpiScaConfirmation(UpdateConsentPsuDataReq request) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getConsentId());
        accountConfirmation.setPsuId(request.getPsuId());
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    private CmsScaMethod mapToCmsScaMethod(SpiScaMethod spiScaMethod) {
        return CmsScaMethod.valueOf(spiScaMethod.name());
    }

    private Xs2aAccountAccess mapToAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new Xs2aAccountAccess(
                                spiXs2aAccountMapper.mapToXs2aAccountReferences(aa.getAccounts()),
                                spiXs2aAccountMapper.mapToXs2aAccountReferences(aa.getBalances()),
                                spiXs2aAccountMapper.mapToXs2aAccountReferences(aa.getTransactions()),
                                mapToAccountAccessType(aa.getAvailableAccounts()),
                                mapToAccountAccessType(aa.getAllPsd2()))
                   )
                   .orElse(null);
    }

    private Xs2aAccountAccessType mapToAccountAccessType(SpiAccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> Xs2aAccountAccessType.valueOf(at.name()))
                   .orElse(null);
    }

    private AisAccountAccessInfo mapToAisAccountAccessInfo(Xs2aAccountAccess access) {
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

    private List<AccountInfo> mapToListAccountInfo(List<Xs2aAccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToAccountInfo(Xs2aAccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }
}
