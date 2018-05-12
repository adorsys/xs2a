/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.isEmpty;

@Service
@AllArgsConstructor
public class ConsentService {
    private final String consentsLinkRedirectToSource;
    private final ConsentSpi consentSpi;
    private final ConsentMapper consentMapper;

    public ResponseObject<CreateConsentResp> createAccountConsentsWithResponse(CreateConsentReq createAccountConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {

        String consentId = createAccountConsentsAndReturnId(createAccountConsentRequest, withBalance, tppRedirectPreferred, psuId);
        return isBlank(consentId)
               ? ResponseObject.<CreateConsentResp>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
               : ResponseObject.<CreateConsentResp>builder().body(new CreateConsentResp(TransactionStatus.RCVD, consentId, null, getLinkToConsent(consentId), null)).build();
    }

    public String createAccountConsentsAndReturnId(CreateConsentReq accountInformationConsentRequest, boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        return consentSpi.createAccountConsents(consentMapper.mapToSpiCreateConsentRequest(accountInformationConsentRequest), withBalance, tppRedirectPreferred, psuId);
    }

    public ResponseObject<TransactionStatus> getAccountConsentsStatusById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
               ? ResponseObject.<TransactionStatus>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
               : ResponseObject.<TransactionStatus>builder().body(consent.getTransactionStatus()).build();
    }

    public ResponseObject<AccountConsent> getAccountConsentsById(String consentId) {
        AccountConsent consent = consentMapper.mapFromSpiAccountConsent(consentSpi.getAccountConsentById(consentId));
        return isEmpty(consent)
               ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build()
               : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        boolean present = getAccountConsentsById(consentId).getBody() != null;
        if (present) {
            consentSpi.deleteAccountConsentsById(consentId);
        }

        return present
               ? ResponseObject.<Void>builder().build()
               : ResponseObject.<Void>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageCode.RESOURCE_UNKNOWN_404))).build();
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
