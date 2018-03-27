package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountAccessType;
import de.adorsys.aspsp.xs2a.domain.ais.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.account.AccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.CreateConsentRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class ConsentMapper {
    public de.adorsys.aspsp.xs2a.domain.TransactionStatus mapGetAccountConsentStatusById(TransactionStatus transactionStatus){
        return Optional.ofNullable(transactionStatus)
        .map(ts-> de.adorsys.aspsp.xs2a.domain.TransactionStatus.valueOf(ts.name()))
        .orElse(null);
    }
    public CreateConsentRequest mapSpiCreateConsentRequest(CreateConsentReq consentReq) {
        return Optional.ofNullable(consentReq)
        .map(consentRe -> new CreateConsentRequest(mapSpiAccountAccess(consentReq.getAccess()),
        consentReq.isRecurringIndicator(), consentReq.getValidUntil(),
        consentReq.getFrequencyPerDay(), consentReq.isCombinedServiceIndicator()))
        .orElse(null);
    }

    public de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent mapGetAccountConsent(AccountConsent accountConsent) {
        return Optional.ofNullable(accountConsent)
        .map(accountConsen -> new de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent(
        accountConsent.getId(), mapAccountAccess(accountConsent.getAccess()),
        accountConsent.isRecurringIndicator(), accountConsent.getValidUntil(),
        accountConsent.getFrequencyPerDay(), accountConsent.getLastActionDate(),
        de.adorsys.aspsp.xs2a.domain.TransactionStatus.valueOf(accountConsent.getTransactionStatus().name()), ConsentStatus.valueOf(accountConsent.getConsentStatus().name()),
        accountConsent.isWithBalance(), accountConsent.isTppRedirectPreferred()))
        .orElse(null);
    }

    //Domain
    private de.adorsys.aspsp.xs2a.domain.ais.consent.AccountAccess mapAccountAccess(AccountAccess access) {
        return Optional.ofNullable(access)
        .map(ar -> {
            de.adorsys.aspsp.xs2a.domain.ais.consent.AccountAccess accountAccess = new de.adorsys.aspsp.xs2a.domain.ais.consent.AccountAccess();
            accountAccess.setAccounts(mapAccountReferences(ar.getAccounts()));
            accountAccess.setBalances(mapAccountReferences(ar.getBalances()));
            accountAccess.setTransactions(mapAccountReferences(ar.getTransactions()));
            accountAccess.setAvailableAccounts(mapAccountAccessType(ar.getAvailableAccounts()));
            accountAccess.setAllPsd2(mapAccountAccessType(ar.getAllPsd2()));
            return accountAccess;
        })
        .orElse(null);
    }

    private AccountReference[] mapAccountReferences(List<de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference> references) {
        if (references == null) {
            return null;
        }

        return references.stream().map(this::mapAccountReference).toArray(AccountReference[]::new);
    }

    private AccountReference mapAccountReference(de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference reference) {
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

    private AccountAccessType mapAccountAccessType(de.adorsys.aspsp.xs2a.spi.domain.consent.AccountAccessType accessType) {
        if (accessType==null){
            return null;
        }
        else {
            return AccountAccessType.valueOf(accessType.name());
        }
    }

    //Spi

    private AccountAccess mapSpiAccountAccess(de.adorsys.aspsp.xs2a.domain.ais.consent.AccountAccess access) {
        return Optional.ofNullable(access)
        .map(ar -> {
            AccountAccess accountAccess = new AccountAccess();
            accountAccess.setAccounts(mapSpiAccountReferences(ar.getAccounts()));
            accountAccess.setBalances(mapSpiAccountReferences(ar.getBalances()));
            accountAccess.setTransactions(mapSpiAccountReferences(ar.getTransactions()));
            accountAccess.setAvailableAccounts(mapSpiAccountAccessType(ar.getAvailableAccounts()));
            accountAccess.setAllPsd2(mapSpiAccountAccessType(ar.getAllPsd2()));
            return accountAccess;
        })
        .orElse(null);
    }

    private List<de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference> mapSpiAccountReferences(AccountReference[] references) {
        if (references == null) {
            return null;
        }

        return Arrays.stream(references).map(this::mapSpiAccountReference).collect(Collectors.toList());
    }

    private de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference mapSpiAccountReference(AccountReference reference) {
        return Optional.of(reference)
        .map(re->new de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference(
        reference.getAccountId(),
        reference.getIban(),
        reference.getBban(),
        reference.getPan(),
        reference.getMaskedPan(),
        reference.getMsisdn(),
        reference.getCurrency())).orElse(null);
    }

    private de.adorsys.aspsp.xs2a.spi.domain.consent.AccountAccessType mapSpiAccountAccessType(AccountAccessType accessType) {
        if (accessType==null){
            return null;
        }
        else {
            return de.adorsys.aspsp.xs2a.spi.domain.consent.AccountAccessType.valueOf(accessType.name());
        }
    }
}
