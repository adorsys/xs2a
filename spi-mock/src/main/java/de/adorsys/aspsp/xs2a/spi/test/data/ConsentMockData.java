package de.adorsys.aspsp.xs2a.spi.test.data;

import de.adorsys.aspsp.xs2a.spi.domain.Consent;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;

import java.util.HashMap;
import java.util.UUID;

public class ConsentMockData {

    private static HashMap<String, Consent> consentMap = new HashMap<String, Consent>();

    public static String createAicRequest(CreateConsentReq aicRequest,
                                          boolean withBalance, boolean tppRedirectPreferred) {

        String consentId = generateConsentId();
        consentMap.put(consentId, new Consent(consentId, aicRequest, withBalance, tppRedirectPreferred));

        return consentId;
    }

    public static CreateConsentReq getAicRequest(String consentId) {
        CreateConsentReq consent = null;
        
        if (consentMap.get(consentId)!=null) {
           consent = consentMap.get(consentId).getAicRequest();
        }
        
        return consent;
    }

    private static String generateConsentId() {
        return UUID.randomUUID().toString();
    }
}
