package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;

public class ConsentSpiImpl implements ConsentSpi {

    @Override
    public String createAicRequest(AccountInformationConsentRequestBody accountInformationConsentRequest,
                                   boolean withBalance, boolean tppRedirectPreferred) {

        return ConsentMockData.createAicRequest(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }

    @Override
    public AccountInformationConsentRequestBody getAicRequest(String consentId) {
        return ConsentMockData.getAicRequest(consentId);
    }
}
