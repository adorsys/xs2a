package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class ConsentMapper {
    public TransactionStatus mapGetAccountConsentStatusById(SpiTransactionStatus spiTransactionStatus){
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts-> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }

    public SpiCreateConsentRequest mapSpiCreateConsentRequest(CreateConsentReq consentReq) {
        return Optional.ofNullable(consentReq)
        .map(consentRe -> new SpiCreateConsentRequest(mapSpiAccountAccess(consentReq.getAccess()),
        consentReq.isRecurringIndicator(), consentReq.getValidUntil(),
        consentReq.getFrequencyPerDay(), consentReq.isCombinedServiceIndicator()))
        .orElse(null);
    }

    public AccountConsent mapGetAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
        .map(accountConsen -> new AccountConsent(
        spiAccountConsent.getId(), mapAccountAccess(spiAccountConsent.getAccess()),
        spiAccountConsent.isRecurringIndicator(), spiAccountConsent.getValidUntil(),
        spiAccountConsent.getFrequencyPerDay(), spiAccountConsent.getLastActionDate(),
        de.adorsys.aspsp.xs2a.domain.TransactionStatus.valueOf(spiAccountConsent.getSpiTransactionStatus().name()),
        ConsentStatus.valueOf(spiAccountConsent.getSpiConsentStatus().name()),
        spiAccountConsent.isWithBalance(), spiAccountConsent.isTppRedirectPreferred()))
        .orElse(null);
    }

    //Domain
    private AccountAccess mapAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
        .map(ar -> {
            AccountAccess accountAccess = new AccountAccess();
            accountAccess.setAccounts(mapAccountReferences(ar.getAccounts()));
            accountAccess.setBalances(mapAccountReferences(ar.getBalances()));
            accountAccess.setTransactions(mapAccountReferences(ar.getTransactions()));
            accountAccess.setAvailableAccounts(mapAccountAccessType(ar.getAvailableAccounts()));
            accountAccess.setAllPsd2(mapAccountAccessType(ar.getAllPsd2()));
            return accountAccess;
        })
        .orElse(null);
    }

    private AccountReference[] mapAccountReferences(List<SpiAccountReference> references) {
        if (references == null) {
            return null;
        }

        return references.stream().map(this::mapAccountReference).toArray(AccountReference[]::new);
    }

    private AccountReference mapAccountReference(SpiAccountReference reference) {
        return Optional.ofNullable(reference)
        .map(ar -> {
            AccountReference accountReference = new AccountReference();
            accountReference.setAccountId(ar.getAccountId());
            accountReference.setIban(ar.getIban());
            accountReference.setBban(ar.getBban());
            accountReference.setPan(ar.getPan());
            accountReference.setMaskedPan(ar.getMaskedPan());
            accountReference.setMsisdn(ar.getMsisdn());
            accountReference.setCurrency(ar.getCurrency());

            return accountReference;
        }).orElse(null);
    }

    private AccountAccessType mapAccountAccessType(SpiAccountAccessType accessType) {
        if (accessType==null){
            return null;
        }
        else {
            return AccountAccessType.valueOf(accessType.name());
        }
    }

    //Spi

    private SpiAccountAccess mapSpiAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access)
        .map(ar -> {
            SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
            spiAccountAccess.setAccounts(mapSpiAccountReferences(ar.getAccounts()));
            spiAccountAccess.setBalances(mapSpiAccountReferences(ar.getBalances()));
            spiAccountAccess.setTransactions(mapSpiAccountReferences(ar.getTransactions()));
            spiAccountAccess.setAvailableAccounts(mapSpiAccountAccessType(ar.getAvailableAccounts()));
            spiAccountAccess.setAllPsd2(mapSpiAccountAccessType(ar.getAllPsd2()));
            return spiAccountAccess;
        })
        .orElse(null);
    }

    private List<SpiAccountReference> mapSpiAccountReferences(AccountReference[] references) {
        if (references == null) {
            return null;
        }

        return Arrays.stream(references).map(this::mapSpiAccountReference).collect(Collectors.toList());
    }

    private SpiAccountReference mapSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
        .map(re->new SpiAccountReference(
        reference.getAccountId(),
        reference.getIban(),
        reference.getBban(),
        reference.getPan(),
        reference.getMaskedPan(),
        reference.getMsisdn(),
        reference.getCurrency())).orElse(null);
    }

    private SpiAccountAccessType mapSpiAccountAccessType(AccountAccessType accessType) {
        if (accessType==null){
            return null;
        }
        else {
            return SpiAccountAccessType.valueOf(accessType.name());
        }
    }
}
