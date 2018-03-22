package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.AccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.CreateConsentRequest;

public interface ConsentSpi {
    String createAccountConsents(CreateConsentRequest accountInformationConsentRequest,
                                 boolean withBalance, boolean tppRedirectPreferred);


    TransactionStatus getAccountConsentStatusById(String consentId);

    AccountConsent getAccountConsentById(String consentId);

    void deleteAccountConsentsById(String consentId);
}
