/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.link;

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.controller.AccountController;
import de.adorsys.psd2.xs2a.web.link.AccountDetailsLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountAspectService extends BaseAspectService<AccountController> {

    @Autowired
    public AccountAspectService(AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(aspspProfileServiceWrapper);
    }

    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetailsAspect(ResponseObject<Xs2aAccountDetailsHolder> result) {
        if (!result.hasError()) {
            Xs2aAccountDetailsHolder body = result.getBody();
            Xs2aAccountDetails accountDetails = body.getAccountDetails();
            accountDetails.setLinks(new AccountDetailsLinks(getHttpUrl(), accountDetails.getResourceId(),
                                                            body.getAisConsent()));
        }
        return result;
    }

    public ResponseObject<Xs2aAccountListHolder> getAccountDetailsListAspect(ResponseObject<Xs2aAccountListHolder> result) {
        if (!result.hasError()) {
            Xs2aAccountListHolder body = result.getBody();
            List<Xs2aAccountDetails> accountDetails = body.getAccountDetails();
            if (body.getAisConsent().getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS) {
                accountDetails.forEach(acc -> acc.setLinks(null));
            } else {
                accountDetails.forEach(acc -> acc.setLinks(new AccountDetailsLinks(getHttpUrl(), acc.getResourceId(),
                                                                                   body.getAisConsent())));
            }
        }
        return result;
    }
}
