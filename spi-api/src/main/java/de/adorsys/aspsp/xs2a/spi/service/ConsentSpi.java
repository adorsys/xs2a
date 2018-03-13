package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;

public interface ConsentSpi {
    String createAccountConsents(CreateConsentReq accountInformationConsentRequest,
                                 boolean withBalance, boolean tppRedirectPreferred);
    
    AccountConsents getAccountConsentsById(String consentId);
}
