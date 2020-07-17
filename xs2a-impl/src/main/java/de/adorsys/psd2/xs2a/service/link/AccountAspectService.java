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
