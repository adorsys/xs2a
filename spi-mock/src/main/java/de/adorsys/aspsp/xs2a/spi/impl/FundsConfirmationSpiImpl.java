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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.aspsp.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

@Component
@AllArgsConstructor
public class FundsConfirmationSpiImpl implements FundsConfirmationSpi {
    private final RestTemplate restTemplate;
    private final RemoteSpiUrls remoteSpiUrls;

    @Override
    public SpiAccountDetails getRequestedAccountDetails(SpiFundsConfirmationRequest request) {
        ResponseEntity<SpiAccountDetails> responseEntity = restTemplate.getForEntity(remoteSpiUrls.getUrl("getAccountByIban"), SpiAccountDetails.class, getRequestedIban(request), getRequestedCurrency(request));
        return responseEntity.getStatusCode() == OK ? responseEntity.getBody() : null;
    }

    private String getRequestedIban(SpiFundsConfirmationRequest request) {
        return Optional.ofNullable(request.getPsuAccount())
            .map(psu -> psu.getIban())
            .orElse("");
    }

    private String getRequestedCurrency(SpiFundsConfirmationRequest request) {
        return Optional.ofNullable(request.getPsuAccount().getCurrency())
            .map(cur -> cur.getCurrencyCode())
            .orElse("");
    }

}
