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

package de.adorsys.aspsp.xs2a.service.consent;

import de.adorsys.aspsp.xs2a.config.rest.consent.AspspConsentDataRemoteUrls;
import de.adorsys.aspsp.xs2a.config.rest.consent.PisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.domain.Xs2aConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PisConsentDataService extends ConsentDataService {
    private PisConsentRemoteUrls pisConsentRemoteUrls;

    public PisConsentDataService(RestTemplate consentRestTemplate, PisConsentRemoteUrls pisConsentRemoteUrls) {
        super(consentRestTemplate);
        this.pisConsentRemoteUrls = pisConsentRemoteUrls;
    }

    public AspspConsentData getAspspConsentDataByPaymentId(String paymentId) {
        return mapToAspspConsentData(consentRestTemplate.getForEntity(getRemoteUrl().getAspspConsentData(), Xs2aConsentData.class, paymentId).getBody());
    }

    @Override
    protected AspspConsentDataRemoteUrls getRemoteUrl() {
        return pisConsentRemoteUrls;
    }
}
