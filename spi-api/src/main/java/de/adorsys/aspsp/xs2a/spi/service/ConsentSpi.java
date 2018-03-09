package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;

public interface ConsentSpi {
    String createAicRequest(AccountInformationConsentRequestBody accountInformationConsentRequest,
                            boolean withBalance, boolean tppRedirectPreferred);

    AccountInformationConsentRequestBody getAicRequest(String consentId);
}
