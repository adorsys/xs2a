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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ConsentMapper {
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
                       TransactionStatus.valueOf(ac.getSpiTransactionStatus().name()),
                       ConsentStatus.valueOf(ac.getSpiConsentStatus().name()),
                       ac.isWithBalance(), ac.isTppRedirectPreferred()))
                   .orElse(null);
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

    private List<AccountReference> mapToAccountReferenceList(List<SpiAccountReference> references) {
        if (references == null) {
            return null;
        }

        return references.stream().map(this::mapToAccountReference).collect(Collectors.toList());
    }

    private AccountReference mapToAccountReference(SpiAccountReference reference) {
        return Optional.ofNullable(reference)
               .map(ar -> {
                   AccountReference accountReference = new AccountReference();
                   accountReference.setIban(ar.getIban());
                   accountReference.setBban(ar.getBban());
                   accountReference.setPan(ar.getPan());
                   accountReference.setMaskedPan(ar.getMaskedPan());
                   accountReference.setMsisdn(ar.getMsisdn());
                   accountReference.setCurrency(ar.getCurrency());

                       return accountReference;
                   }).orElse(null);
    }

    private AccountAccessType mapToAccountAccessType(SpiAccountAccessType accessType) {
        if (accessType == null) {
            return null;
        } else {
            return AccountAccessType.valueOf(accessType.name());
        }
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

    private List<SpiAccountReference> mapToSpiAccountReferenceList(List<AccountReference> references) {
        if (references == null) {
            return null;
        }

        return references.stream().map(this::mapToSpiAccountReference).collect(Collectors.toList());
    }

    public SpiAccountReference mapToSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
               .map(ar -> new SpiAccountReference(
               ar.getIban(),
               ar.getBban(),
               ar.getPan(),
               ar.getMaskedPan(),
               ar.getMsisdn(),
               ar.getCurrency())).orElse(null);
    }

    private SpiAccountAccessType mapToSpiAccountAccessType(AccountAccessType accessType) {
        if (accessType == null) {
            return null;
        } else {
            return SpiAccountAccessType.valueOf(accessType.name());
        }
    }

    public SpiAccountConsent mapToSpiAccountConsent(AccountConsent consent) {
        return new SpiAccountConsent(consent.getId(), mapToSpiAccountAccess(consent.getAccess()), consent.isRecurringIndicator(),
            consent.getValidUntil(), consent.getFrequencyPerDay(), consent.getLastActionDate(), mapToSpiTransactionStatus(consent.getTransactionStatus()),
            mapToSpiConsentStatus(consent.getConsentStatus()), consent.isWithBalance(), consent.isTppRedirectPreferred());
    }

    private SpiConsentStatus mapToSpiConsentStatus(ConsentStatus consentStatus) {
    return SpiConsentStatus.valueOf(consentStatus.name());
    }

    private SpiTransactionStatus mapToSpiTransactionStatus(TransactionStatus transactionStatus) {
        return SpiTransactionStatus.valueOf(transactionStatus.name());
    }
}
