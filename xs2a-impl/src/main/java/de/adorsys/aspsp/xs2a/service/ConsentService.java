package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.spi.domain.Links;
import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentResponseBody;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConsentService {

    @Value("${application.ais.consents.link.redirect-to}")
    private String redirectToLink;

    @Autowired
    private ConsentSpi consentSpi;

    public AccountInformationConsentResponseBody createAicRequest(AccountInformationConsentRequestBody accountInformationConsentRequest, boolean withBalance, boolean tppRedirectPreferred) {

        String consentId = consentSpi.createAicRequest(accountInformationConsentRequest, withBalance, tppRedirectPreferred);

        AccountInformationConsentResponseBody aicResponse = new AccountInformationConsentResponseBody();
        aicResponse.set_links(getLinks(consentId));
        aicResponse.setConsentId(consentId);
        aicResponse.setTransactionStatus(TransactionStatus.RCVD);

        return aicResponse;
    }

     public AccountInformationConsentRequestBody getAicRequest(String consentId){
        return consentSpi.getAicRequest(consentId);
    }

    private Links getLinks(String consentId) {
        Links links = new Links();

        // Response in case of the OAuth2 approach
        // todo figure out when we should return  OAuth2 response
        //String selfLink = linkTo(ConsentInformationController.class).slash(consentId).toString();
        //links.setSelf(selfLink);

        // Response in case of a redirect
        String redirectLink = redirectToLink + "/" + consentId;
        links.setRedirect(redirectLink);

        return links;
    }
}
