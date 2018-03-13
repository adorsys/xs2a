package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;

public interface ConsentSpi {
    String createAicRequest(CreateConsentReq accountInformationConsentRequest,
                            boolean withBalance, boolean tppRedirectPreferred);

    CreateConsentReq getAicRequest(String consentId);
}
