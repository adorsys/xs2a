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

import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsentMapper {
    private final AccountMapper accountMapper;

    public AisConsentRequest mapToAisConsentRequest(CreateConsentReq req, String psuId, String tppId) {
        return Optional.ofNullable(req)
                   .map(r -> {
                       AisConsentRequest request = new AisConsentRequest();
                       request.setPsuId(psuId);
                       request.setTppId(tppId);
                       request.setFrequencyPerDay(r.getFrequencyPerDay());
                       request.setAccess(mapToAisAccountAccessInfo(req.getAccess()));
                       request.setValidUntil(r.getValidUntil());
                       request.setRecurringIndicator(r.isRecurringIndicator());
                       request.setCombinedServiceIndicator(r.isCombinedServiceIndicator());

                       return request;
                   })
                   .orElse(null);
    }

    private AisAccountAccessInfo mapToAisAccountAccessInfo(AccountAccess access) {
        AisAccountAccessInfo accessInfo = new AisAccountAccessInfo();
        accessInfo.setAccounts(Optional.ofNullable(access.getAccounts())
                                   .map(this::mapToListAccountInfo)
                                   .orElse(Collections.emptyList()));

        accessInfo.setBalances(Optional.ofNullable(access.getBalances())
                                   .map(this::mapToListAccountInfo)
                                   .orElse(Collections.emptyList()));

        accessInfo.setTransactions(Optional.ofNullable(access.getTransactions())
                                       .map(this::mapToListAccountInfo)
                                       .orElse(Collections.emptyList()));

        accessInfo.setAvailableAccounts(Optional.ofNullable(access.getAvailableAccounts())
                                            .map(AccountAccessType::name)
                                            .orElse(null));
        accessInfo.setAllPsd2(Optional.ofNullable(access.getAllPsd2())
                                  .map(AccountAccessType::name)
                                  .orElse(null));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(List<AccountReference> refs) {
        return refs.stream()
                   .map(this::mapToAccountInfo)
                   .collect(Collectors.toList());
    }

    private AccountInfo mapToAccountInfo(AccountReference ref) {
        AccountInfo info = new AccountInfo();
        info.setIban(ref.getIban());
        info.setCurrency(Optional.ofNullable(ref.getCurrency())
                             .map(Currency::getCurrencyCode)
                             .orElse(null));
        return info;
    }

    public TransactionStatus mapToTransactionStatus(SpiTransactionStatus spiTransactionStatus) {
        return Optional.ofNullable(spiTransactionStatus)
                   .map(ts -> TransactionStatus.valueOf(ts.name()))
                   .orElse(null);
    }

    public SpiCreateConsentRequest mapToSpiCreateConsentRequest(CreateConsentReq consentReq) {
        return Optional.ofNullable(consentReq)
                   .map(cr -> new SpiCreateConsentRequest(mapToSpiAccountAccess(cr.getAccess()),
                       cr.isRecurringIndicator(), cr.getValidUntil(),
                       cr.getFrequencyPerDay(), cr.isCombinedServiceIndicator()))
                   .orElse(null);
    }

    public AccountConsent mapToAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
                   .map(ac -> new AccountConsent(
                       ac.getId(), mapToAccountAccess(ac.getAccess()),
                       ac.isRecurringIndicator(), ac.getValidUntil(),
                       ac.getFrequencyPerDay(), ac.getLastActionDate(),
                       ConsentStatus.valueOf(ac.getSpiConsentStatus().name()),
                       ac.isWithBalance(), ac.isTppRedirectPreferred()))
                   .orElse(null);
    }

    public Optional<ConsentStatus> mapToConsentStatus(SpiConsentStatus spiConsentStatus) {
        return Optional.ofNullable(spiConsentStatus)
                   .map(status -> ConsentStatus.valueOf(status.name()));
    }

    //Domain
    private AccountAccess mapToAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new AccountAccess(
                                accountMapper.mapToAccountReferences(aa.getAccounts()),
                                accountMapper.mapToAccountReferences(aa.getBalances()),
                                accountMapper.mapToAccountReferences(aa.getTransactions()),
                                mapToAccountAccessType(aa.getAvailableAccounts()),
                                mapToAccountAccessType(aa.getAllPsd2()))
                   )
                   .orElse(null);
    }

    private AccountAccessType mapToAccountAccessType(SpiAccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> AccountAccessType.valueOf(at.name()))
                   .orElse(null);
    }

    //Spi
    private SpiAccountAccess mapToSpiAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa -> {
                       SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
                       spiAccountAccess.setAccounts(accountMapper.mapToSpiAccountReferences(aa.getAccounts()));
                       spiAccountAccess.setBalances(accountMapper.mapToSpiAccountReferences(aa.getBalances()));
                       spiAccountAccess.setTransactions(accountMapper.mapToSpiAccountReferences(aa.getTransactions()));
                       spiAccountAccess.setAvailableAccounts(mapToSpiAccountAccessType(aa.getAvailableAccounts()));
                       spiAccountAccess.setAllPsd2(mapToSpiAccountAccessType(aa.getAllPsd2()));
                       return spiAccountAccess;
                   })
                   .orElse(null);
    }

    private SpiAccountAccessType mapToSpiAccountAccessType(AccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> SpiAccountAccessType.valueOf(at.name()))
                   .orElse(null);

    }
}
