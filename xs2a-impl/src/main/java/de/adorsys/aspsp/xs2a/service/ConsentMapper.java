package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class ConsentMapper {
    public TransactionStatus mapFromSpiTransactionStatus(SpiTransactionStatus spiTransactionStatus){
        return Optional.ofNullable(spiTransactionStatus)
        .map(ts-> TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }

    public SpiCreateConsentRequest mapSpiCreateConsentRequest(CreateConsentReq consentReq) {
        return Optional.ofNullable(consentReq)
        .map(cr -> new SpiCreateConsentRequest(mapSpiAccountAccess(cr.getAccess()),
        cr.isRecurringIndicator(), cr.getValidUntil(),
        cr.getFrequencyPerDay(), cr.isCombinedServiceIndicator()))
        .orElse(null);
    }

    public AccountConsent mapGetAccountConsent(SpiAccountConsent spiAccountConsent) {
        return Optional.ofNullable(spiAccountConsent)
        .map(ac -> new AccountConsent(
        ac.getId(), mapAccountAccess(ac.getAccess()),
        ac.isRecurringIndicator(), ac.getValidUntil(),
        ac.getFrequencyPerDay(), ac.getLastActionDate(),
        TransactionStatus.valueOf(ac.getSpiTransactionStatus().name()),
        ConsentStatus.valueOf(ac.getSpiConsentStatus().name()),
        ac.isWithBalance(), ac.isTppRedirectPreferred()))
        .orElse(null);
    }

    //Domain
    private AccountAccess mapAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
        .map(aa -> {
            AccountAccess accountAccess = new AccountAccess();
            accountAccess.setAccounts(mapAccountReferences(aa.getAccounts()));
            accountAccess.setBalances(mapAccountReferences(aa.getBalances()));
            accountAccess.setTransactions(mapAccountReferences(aa.getTransactions()));
            accountAccess.setAvailableAccounts(mapAccountAccessType(aa.getAvailableAccounts()));
            accountAccess.setAllPsd2(mapAccountAccessType(aa.getAllPsd2()));
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
        .map(aa -> {
            SpiAccountAccess spiAccountAccess = new SpiAccountAccess();
            spiAccountAccess.setAccounts(mapSpiAccountReferences(aa.getAccounts()));
            spiAccountAccess.setBalances(mapSpiAccountReferences(aa.getBalances()));
            spiAccountAccess.setTransactions(mapSpiAccountReferences(aa.getTransactions()));
            spiAccountAccess.setAvailableAccounts(mapSpiAccountAccessType(aa.getAvailableAccounts()));
            spiAccountAccess.setAllPsd2(mapSpiAccountAccessType(aa.getAllPsd2()));
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

    public SpiAccountReference mapSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
        .map(ar->new SpiAccountReference(
        ar.getAccountId(),
        ar.getIban(),
        ar.getBban(),
        ar.getPan(),
        ar.getMaskedPan(),
        ar.getMsisdn(),
        ar.getCurrency())).orElse(null);
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
