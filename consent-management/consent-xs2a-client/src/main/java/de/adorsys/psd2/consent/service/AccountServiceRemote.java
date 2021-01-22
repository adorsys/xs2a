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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.service.AccountServiceEncrypted;
import de.adorsys.psd2.consent.config.AccountRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceRemote implements AccountServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AccountRemoteUrls accountRemoteUrls;

    @Override
    public boolean saveTransactionParameters(String consentId, String resourceId, UpdateTransactionParametersRequest transactionParameters) {
        try {
            ResponseEntity<Boolean> response = consentRestTemplate.exchange(accountRemoteUrls.saveTransactionParameters(),
                                                                            HttpMethod.PUT, new HttpEntity<>(transactionParameters), Boolean.class, consentId, resourceId);

            return BooleanUtils.isTrue(response.getBody());
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't save number of transactions to CMS");
        }

        return false;
    }
}
