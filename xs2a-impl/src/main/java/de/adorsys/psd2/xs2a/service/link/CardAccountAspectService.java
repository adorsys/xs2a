/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.*;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.CardAccountController;
import de.adorsys.psd2.xs2a.web.link.CardAccountDetailsLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportCardDownloadLinks;
import de.adorsys.psd2.xs2a.web.link.TransactionsReportCardLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardAccountAspectService extends BaseAspectService<CardAccountController> {

    @Autowired
    public CardAccountAspectService(AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(aspspProfileServiceWrapper);
    }

    public ResponseObject<Xs2aCardAccountListHolder> getCardAccountList(ResponseObject<Xs2aCardAccountListHolder> result) {
        if (!result.hasError()) {
            Xs2aCardAccountListHolder body = result.getBody();
            List<Xs2aCardAccountDetails> cardAccountDetails = body.getCardAccountDetails();
            AisConsent aisConsent = body.getAisConsent();
            if (aisConsent.getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
                cardAccountDetails.forEach(acc -> acc.setLinks(null));
            } else {
                cardAccountDetails.forEach(acc -> setLinksForCardAccountDetails(acc, body.getAisConsent()));
            }
        }
        return result;
    }

    public ResponseObject<Xs2aCardAccountDetailsHolder> getCardAccountDetails(ResponseObject<Xs2aCardAccountDetailsHolder> result) {
        if (!result.hasError()) {
            Xs2aCardAccountDetailsHolder body = result.getBody();
            setLinksForCardAccountDetails(body.getCardAccountDetails(), body.getAisConsent());
        }
        return result;
    }

    public ResponseObject<Xs2aCardTransactionsReport> getTransactionsReportByPeriod(ResponseObject<Xs2aCardTransactionsReport> result,
                                                                                    Xs2aCardTransactionsReportByPeriodRequest request) {
        if (!result.hasError()) {
            Xs2aCardTransactionsReport transactionsReport = result.getBody();
            transactionsReport.setLinks(new TransactionsReportCardDownloadLinks(getHttpUrl(), request.getAccountId(), false, transactionsReport.getDownloadId()));
            Xs2aCardAccountReport cardAccountReport = transactionsReport.getCardAccountReport();
            if (cardAccountReport != null) {
                cardAccountReport.setLinks(new TransactionsReportCardLinks(getHttpUrl(), request.getAccountId(), false));
            }
        }
        return result;
    }

    private void setLinksForCardAccountDetails(Xs2aCardAccountDetails cardAccountDetails, AisConsent aisConsent) {
        String url = getHttpUrl();
        String id = cardAccountDetails.getResourceId();
        CardAccountDetailsLinks cardAccountDetailsLinks = new CardAccountDetailsLinks(url, id, aisConsent);
        cardAccountDetails.setLinks(cardAccountDetailsLinks);
    }
}
