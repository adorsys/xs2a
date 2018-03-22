package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.AccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.CreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;

public class ConsentSpiImpl implements ConsentSpi {

    @Override
    public String createAccountConsents(CreateConsentRequest accountInformationConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred) {

        return ConsentMockData.createAccountConsent(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }

    @Override
    public TransactionStatus getAccountConsentStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);

    }
    @Override
    public AccountConsent getAccountConsentById(String consentId) {
        return ConsentMockData.getAccountConsent(consentId);
    }

    @Override
    public void deleteAccountConsentsById(String consentId) {
        ConsentMockData.deleteAccountConcent(consentId);
    }
}
