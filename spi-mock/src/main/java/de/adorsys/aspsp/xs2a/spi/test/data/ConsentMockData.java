package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.account.AccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.ConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.CreateConsentRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConsentMockData {

    private static Map<String, AccountConsent> consentMap = new HashMap<>();

    public static String createAccountConsent(CreateConsentRequest aicRequest,
                                              boolean withBalance, boolean tppRedirectPreferred) {

        String consentId = generateConsentId();
        consentMap.put(consentId, new AccountConsent(consentId,
                aicRequest.getAccess(),
                aicRequest.isRecurringIndicator(),
                aicRequest.getValidUntil(),
                aicRequest.getFrequencyPerDay(),
                new Date(),
                TransactionStatus.ACTC,
                ConsentStatus.VALID,
                withBalance,
                tppRedirectPreferred
            )
        );

        return consentId;
    }

    public static TransactionStatus getAccountConsentsStatus(String consentId) {
        AccountConsent accountConsents = consentMap.get(consentId);
        if (accountConsents!=null) {
           return accountConsents.getTransactionStatus();
        }
       return null;
    }

    public static AccountConsent getAccountConsent(String consentId) {
        return consentMap.get(consentId);
    }

    public static void deleteAccountConcent(String consentId){
        consentMap.remove(consentId);
    }

    private static String generateConsentId() {
        return UUID.randomUUID().toString();
    }
}
