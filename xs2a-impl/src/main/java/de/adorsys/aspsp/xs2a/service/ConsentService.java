package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsentService {
    private String consentsLinkRedirectToSource;
    private ConsentSpi consentSpi;

    @Autowired
    public ConsentService(ConsentSpi consentSpi, String consentsLinkRedirectToSource) {
        this.consentSpi = consentSpi;
        this.consentsLinkRedirectToSource = consentsLinkRedirectToSource;
    }

    public CreateConsentResp createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred) {

        String consentId = createAccountConsentsAndReturnId(createAccountConsentRequest, withBalance, tppRedirectPreferred);

        return new CreateConsentResp(
        TransactionStatus.RCVD,
        consentId,
        null,
        getLinkToConsent(consentId),
        null);
    }

    public String createAccountConsentsAndReturnId(CreateConsentReq accountInformationConsentRequest, boolean withBalance, boolean tppRedirectPreferred) {
        //TODO #58 Create Mapper by example from AccountService
        return consentSpi.createAccountConsents(accountInformationConsentRequest, withBalance, tppRedirectPreferred);
    }

    public TransactionStatus getAccountConsentsStatusById(String consentId) {
        //TODO #58 Create Mapper by example from AccountService
        return consentSpi.getAccountConsentStatusById(consentId);
    }

    public AccountConsent getAccountConsentsById(String consentId) {
        //TODO #58 Create Mapper by example from AccountService
        return consentSpi.getAccountConsentById(consentId);
    }

    public void deleteAccountConsentsById(String consentId) {
        consentSpi.deleteAccountConsentsById(consentId);
    }

    private Links getLinkToConsent(String consentId) {
        Links linksToConsent = new Links();

        // Response in case of the OAuth2 approach
        // todo figure out when we should return  OAuth2 response
        //String selfLink = linkTo(ConsentInformationController.class).slash(consentId).toString();
        //linksToConsent.setSelf(selfLink);

        // Response in case of a redirect
        String redirectLink = consentsLinkRedirectToSource + "/" + consentId;
        linksToConsent.setRedirect(redirectLink);

        return linksToConsent;
    }
}
