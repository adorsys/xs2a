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

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AccountInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ais.AisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
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
                       request.setValidUntil(LocalDateTime.ofInstant(r.getValidUntil().toInstant(), ZoneId.systemDefault()));
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

        accessInfo.setAvailableAccounts(mapToSpiAccountAccessType(access.getAvailableAccounts()));
        accessInfo.setAllPsd2(mapToSpiAccountAccessType(access.getAllPsd2()));

        return accessInfo;
    }

    private List<AccountInfo> mapToListAccountInfo(AccountReference[] refs) {
        return Arrays.stream(refs)
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
                                mapToAccountReferenceList(aa.getAccounts()),
                                mapToAccountReferenceList(aa.getBalances()),
                                mapToAccountReferenceList(aa.getTransactions()),
                                mapToAccountAccessType(aa.getAvailableAccounts()),
                                mapToAccountAccessType(aa.getAllPsd2()))
                   )
                   .orElse(null);
    }

    private AccountReference[] mapToAccountReferenceList(List<SpiAccountReference> references) {
        return Optional.ofNullable(references).map(Collection::stream)
                   .map(r -> r
                                 .map(accountMapper::mapToAccountReference)
                                 .toArray(AccountReference[]::new))
                   .orElse(new AccountReference[]{});
    }

    private AccountAccessType mapToAccountAccessType(SpiAccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> AccountAccessType.valueOf(at.name()))
                   .orElse(null);
    }

    //Spi
    public SpiAccountAccess mapToSpiAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa -> {
                       SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
                       spiAccountAccess.setAccounts(mapToSpiAccountReferenceList(aa.getAccounts()));
                       spiAccountAccess.setBalances(mapToSpiAccountReferenceList(aa.getBalances()));
                       spiAccountAccess.setTransactions(mapToSpiAccountReferenceList(aa.getTransactions()));
                       spiAccountAccess.setAvailableAccounts(mapToSpiAccountAccessType(aa.getAvailableAccounts()));
                       spiAccountAccess.setAllPsd2(mapToSpiAccountAccessType(aa.getAllPsd2()));
                       return spiAccountAccess;
                   })
                   .orElse(null);
    }

    private List<SpiAccountReference> mapToSpiAccountReferenceList(AccountReference[] references) {
        return Optional.ofNullable(references)
                   .map(ref -> Arrays.stream(ref)
                                   .map(accountMapper::mapToSpiAccountReference).collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    private SpiAccountAccessType mapToSpiAccountAccessType(AccountAccessType accessType) {
        return Optional.ofNullable(accessType)
                   .map(at -> SpiAccountAccessType.valueOf(at.name()))
                   .orElse(null);

    }
}
