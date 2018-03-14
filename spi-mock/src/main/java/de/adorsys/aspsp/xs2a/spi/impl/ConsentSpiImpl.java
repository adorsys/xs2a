package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;

public class ConsentSpiImpl implements ConsentSpi {
    
    @Override
    public String createAccountConsents(CreateConsentReq accountInformationConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred) {
        
        return ConsentMockData.createAccountConsent(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }
    
    @Override
    public TransactionStatus getAccountConsentsStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);
        
    }@Override
    public AccountConsents getAccountConsentsById(String consentId) {
        return ConsentMockData.getAccountConsent(consentId);
        
    }
}
