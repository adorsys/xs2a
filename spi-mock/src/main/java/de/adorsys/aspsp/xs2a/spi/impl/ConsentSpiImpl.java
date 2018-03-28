package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;

public class ConsentSpiImpl implements ConsentSpi {

    @Override
    public String createAccountConsents(SpiCreateConsentRequest accountInformationConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred) {

        return ConsentMockData.createAccountConsent(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }

    @Override
    public SpiTransactionStatus getAccountConsentStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);

    }
    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return ConsentMockData.getAccountConsent(consentId);
    }

    @Override
    public void deleteAccountConsentsById(String consentId) {
        ConsentMockData.deleteAccountConcent(consentId);
    }
}
