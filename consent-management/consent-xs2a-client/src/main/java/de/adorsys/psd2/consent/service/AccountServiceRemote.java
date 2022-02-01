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
