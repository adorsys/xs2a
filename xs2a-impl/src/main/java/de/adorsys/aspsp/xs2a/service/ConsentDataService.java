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

import de.adorsys.aspsp.xs2a.config.rest.consent.AspspConsentDataRemoteUrls;
import de.adorsys.aspsp.xs2a.domain.Xs2aConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;

@Data
@Service
@RequiredArgsConstructor
public abstract class ConsentDataService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    //TODO use appropriate class for this service, split layers and responsibilities https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

    public AspspConsentData getAspspConsentDataByConsentId(String consentId) {
        Xs2aConsentData xs2aConsentData = consentRestTemplate.getForEntity(getRemoteUrl().getAspspConsentData(), Xs2aConsentData.class, consentId).getBody();
        return new AspspConsentData(xs2aConsentData.getAspspConsentData(), consentId);
    }

    public AspspConsentData getAspspConsentDataByPaymentId(String paymentId) {
        return consentRestTemplate.getForEntity(getRemoteUrl().getAspspConsentData(), AspspConsentData.class, paymentId).getBody();
    }

    public void updateAspspConsentData(AspspConsentData consentData) {
        Optional.ofNullable(consentData)
            .filter(cd -> StringUtils.isNotBlank(cd.getConsentId()))
            .ifPresent(cd -> consentRestTemplate.put(getRemoteUrl().updateAspspConsentData(), new Xs2aConsentData(cd.getAspspConsentData()), cd.getConsentId()));
    }

    protected abstract AspspConsentDataRemoteUrls getRemoteUrl();
}
