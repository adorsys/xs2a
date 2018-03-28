package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;

public interface ConsentSpi {
    String createAccountConsents(SpiCreateConsentRequest accountInformationConsentRequest,
                                 boolean withBalance, boolean tppRedirectPreferred);


    SpiTransactionStatus getAccountConsentStatusById(String consentId);

    SpiAccountConsent getAccountConsentById(String consentId);

    void deleteAccountConsentsById(String consentId);
}
