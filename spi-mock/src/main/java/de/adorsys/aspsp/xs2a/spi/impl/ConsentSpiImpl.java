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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@AllArgsConstructor
public class ConsentSpiImpl implements ConsentSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;

    @Override
    public String createAccountConsents(SpiCreateConsentRequest createConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred, String psuId) {

        String url = remoteSpiUrls.getUrl("getConsents");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.queryParam("withBalance", withBalance);
        builder.queryParam("tppRedirectPreferred", tppRedirectPreferred);
        builder.queryParam("psuId", psuId);

        ResponseEntity<String> response = restTemplate.postForEntity(builder.build().encode().toUri(), createConsentRequest, String.class);
        return response.getBody();
    }

    @Override
    public SpiTransactionStatus getAccountConsentStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);
    }

    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        String url = remoteSpiUrls.getUrl("getConsentById");
        ResponseEntity<SpiAccountConsent> response = restTemplate.getForEntity(url, SpiAccountConsent.class, consentId);

        return response.getBody();
    }

    @Override
    public void deleteAccountConsentsById(String consentId) {
        String url = remoteSpiUrls.getUrl("deleteConsentById");
        restTemplate.delete(url, consentId);
    }
}
