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

package de.adorsys.aspsp.cmsclient.sample;

import de.adorsys.aspsp.cmsclient.cms.CmsServiceInvoker;
import de.adorsys.aspsp.cmsclient.cms.model.ais.CreateAisConsentMethod;
import de.adorsys.aspsp.cmsclient.cms.model.ais.GetAisConsentMethod;
import de.adorsys.aspsp.cmsclient.core.Configuration;
import de.adorsys.aspsp.cmsclient.core.util.HttpUriParams;
import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountConsent;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class CmsExecutor {
    private static final Log logger = LogFactory.getLog(CmsExecutor.class);

    private static final String CMS_BASE_URL = "http://localhost:38080";
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

    public static void main(String[] args) throws IOException, URISyntaxException {
        Configuration configuration = new Configuration(CMS_BASE_URL, CONNECTION_TIMEOUT, CONNECTION_REQUEST_TIMEOUT);

        CmsServiceInvoker cmsServiceInvoker = configuration.getRestServiceInvoker();

        /* Create AIS consent */
        String consentId = null;
        Optional<CreateAisConsentResponse> createAisResponse = Optional.ofNullable(cmsServiceInvoker.invoke(new CreateAisConsentMethod(buildAisConsentRequest())));
        if (createAisResponse.isPresent()) {
            consentId = createAisResponse.get()
                            .getConsentId();
        }
        logger.info("Consent ID: " + consentId);

        /* Get AisConsent by ID */
        HttpUriParams uriParams = HttpUriParams.builder()
                                      .addPathVariable("consent-id", consentId)
                                      .build();
        Optional<AisAccountConsent> aisAccountConsent = Optional.ofNullable(cmsServiceInvoker.invoke(new GetAisConsentMethod(uriParams)));
        aisAccountConsent.ifPresent(consent -> logger.info("Ais account consent status: " + consent.getConsentStatus()));
    }

    private static CreateAisConsentRequest buildAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setFrequencyPerDay(5);
        request.setPsuId("psu-id-1");
        request.setRecurringIndicator(true);
        request.setTppId("tpp-id-1");
        request.setValidUntil(LocalDate.now());
        request.setTppRedirectPreferred(true);
        return request;
    }

    private static AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(singletonList(new AccountInfo("iban-1", "EUR")));
        return info;
    }
}
