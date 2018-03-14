package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.ConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class ConsentMockData {
    
    private static HashMap<String, AccountConsents> consentMap = new HashMap<String, AccountConsents>();
    
    public static String createAccountConsent(CreateConsentReq aicRequest,
                                              boolean withBalance, boolean tppRedirectPreferred) {
        
        String consentId = generateConsentId();
        consentMap.put(consentId, new AccountConsents(consentId,
        aicRequest.getAccess(),
        aicRequest.isRecurringIndicator(),
        aicRequest.getValidUntil(),
        aicRequest.getFrequencyPerDay(),
        new Date(),
        TransactionStatus.ACTC,
        ConsentStatus.VALID,
        withBalance,
        tppRedirectPreferred
        ));
        
        return consentId;
    }
    
    public static TransactionStatus getAccountConsentsStatus(String consentId) {
        AccountConsents accountConsents = consentMap.get(consentId);
        if (accountConsents!=null) {
           return accountConsents.getTransactionStatus();
        }
       return null;
    }
    
    public static AccountConsents getAccountConsent(String consentId) {
        return consentMap.get(consentId);
    }
    
    private static String generateConsentId() {
        return UUID.randomUUID().toString();
    }
}
