package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConsentMockData {

    private static Map<String, SpiAccountConsent> consentMap = new HashMap<>();

    public static String createAccountConsent(SpiCreateConsentRequest aicRequest,
                                              boolean withBalance, boolean tppRedirectPreferred) {

        String consentId = generateConsentId();
        consentMap.put(consentId, new SpiAccountConsent(consentId,
                aicRequest.getAccess(),
                aicRequest.isRecurringIndicator(),
                aicRequest.getValidUntil(),
                aicRequest.getFrequencyPerDay(),
                new Date(),
                SpiTransactionStatus.ACTC,
                SpiConsentStatus.VALID,
                withBalance,
                tppRedirectPreferred
            )
        );

        return consentId;
    }

    public static SpiTransactionStatus getAccountConsentsStatus(String consentId) {
        SpiAccountConsent spiAccountConsents = consentMap.get(consentId);
        if (spiAccountConsents !=null) {
           return spiAccountConsents.getSpiTransactionStatus();
        }
       return null;
    }

    public static SpiAccountConsent getAccountConsent(String consentId) {
        return consentMap.get(consentId);
    }

    public static void deleteAccountConcent(String consentId){
        consentMap.remove(consentId);
    }

    private static String generateConsentId() {
        return UUID.randomUUID().toString();
    }
}
