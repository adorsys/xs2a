package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsents;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.CreateConsentResp;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountConsentsStatusResp;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsentService {
    private String redirectToLink;
    private ConsentSpi consentSpi;
    
    @Autowired
    public ConsentService(ConsentSpi consentSpi, String redirectToLink) {
        this.consentSpi = consentSpi;
        this.redirectToLink = redirectToLink;
    }
    
    public CreateConsentResp createAicRequest(CreateConsentReq accountInformationConsentRequest, boolean withBalance, boolean tppRedirectPreferred) {
        
        String consentId = consentSpi.createAccountConsents(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
        
        CreateConsentResp aicResponse = new CreateConsentResp();
        aicResponse.set_links(getLinkToConsent(consentId));
        aicResponse.setConsentId(consentId);
        aicResponse.setTransactionStatus(TransactionStatus.RCVD);
        
        return aicResponse;
    }
    
    public AccountConsentsStatusResp getAccountConsentsById(String consentId) {
        AccountConsents accountConsents = consentSpi.getAccountConsentsById(consentId);
    
        AccountConsentsStatusResp resp = new AccountConsentsStatusResp(accountConsents.getAccess(),
        accountConsents.isRecurringIndicator(),
        accountConsents.getValidUntil(),
        accountConsents.getFrequencyPerDay(),
        accountConsents.getLastActionDate(),
        accountConsents.getTransactionStatus(),
        accountConsents.getConsentStatus());
        
        return resp;
    }
    
    private Links getLinkToConsent(String consentId) {
        Links linksToConsent = new Links();
        
        // Response in case of the OAuth2 approach
        // todo figure out when we should return  OAuth2 response
        //String selfLink = linkTo(ConsentInformationController.class).slash(consentId).toString();
        //linksToConsent.setSelf(selfLink);
        
        // Response in case of a redirect
        String redirectLink = redirectToLink + "/" + consentId;
        linksToConsent.setRedirect(redirectLink);
        
        return linksToConsent;
    }
}
