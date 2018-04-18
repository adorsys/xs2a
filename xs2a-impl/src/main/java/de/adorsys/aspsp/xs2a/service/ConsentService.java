package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.springframework.util.StringUtils.isEmpty;

@Service
public class ConsentService {
    private String consentsLinkRedirectToSource;
    private ConsentSpi consentSpi;
    private ConsentMapper consentMapper;

    @Autowired
    public ConsentService(ConsentSpi consentSpi, String consentsLinkRedirectToSource, ConsentMapper consentMapper) {
        this.consentSpi = consentSpi;
        this.consentsLinkRedirectToSource = consentsLinkRedirectToSource;
        this.consentMapper = consentMapper;
    }

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {

        String consentId = createAccountConsentsAndReturnId(createAccountConsentRequest, withBalance, tppRedirectPreferred, psuId);
        return isEmpty(consentId)
               ? new ResponseObject<>().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404)))
               : new ResponseObject<>(new CreateConsentResp(TransactionStatus.RCVD, consentId, null, getLinkToConsent(consentId), null));
    }

    public String createAccountConsentsAndReturnId(CreateConsentReq accountInformationConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        return consentSpi.createAccountConsents(consentMapper.mapToSpiCreateConsentRequest(accountInformationConsentRequest), withBalance, tppRedirectPreferred, psuId);
    }

    public TransactionStatus getAccountConsentsStatusById(String consentId) {
        return consentMapper.mapFromSpiTransactionStatus(consentSpi.getAccountConsentStatusById(consentId));
    }

    public AccountConsent getAccountConsentsById(String consentId) {
        return consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
    }

    public boolean deleteAccountConsentsById(String consentId) {
        return consentSpi.deleteAccountConsentsById(consentId);
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
