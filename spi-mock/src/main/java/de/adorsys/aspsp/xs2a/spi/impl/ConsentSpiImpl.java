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

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.AisConsentStatusResponse;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.aspsp.xs2a.service.mapper.AccountMapper;
import de.adorsys.aspsp.xs2a.spi.config.rest.consent.SpiAisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.config.rest.consent.SpiPisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.consent.*;
import de.adorsys.aspsp.xs2a.spi.impl.mapper.SpiAisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.impl.mapper.SpiPisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ConsentSpiImpl implements ConsentSpi {

    @Qualifier("spiConsentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final SpiAisConsentRemoteUrls remoteAisConsentUrls;
    private final SpiPisConsentRemoteUrls remotePisConsentUrls;
    private final SpiAisConsentMapper aisConsentMapper;
    private final SpiPisConsentMapper pisConsentMapper;
    private final AccountSpi accountSpi;
    private final AccountMapper accountMapper;


    /**
     * For detailed description see {@link ConsentSpi#createConsent(SpiCreateAisConsentRequest)}
     */
    @Override
    public String createConsent(SpiCreateAisConsentRequest spiCreateAisConsentRequest) {
        if (isDirectAccessRequest(spiCreateAisConsentRequest) && isInvalidSpiAccountAccessRequest(spiCreateAisConsentRequest.getAccess())) {
            return null;
        }

        CreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToCmsCreateAisConsentRequest(spiCreateAisConsentRequest);
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), createAisConsentRequest, CreateAisConsentResponse.class).getBody();

        return Optional.ofNullable(createAisConsentResponse)
                   .map(CreateAisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#getAccountConsentById(String)}
     */
    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    /**
     * For detailed description see {@link ConsentSpi#getAccountConsentStatusById(String)}
     */
    @Override
    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return aisConsentMapper.mapToSpiConsentStatus(response.getConsentStatus())
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#revokeConsent(String)}
     */
    @Override
    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * For detailed description see {@link ConsentSpi#consentActionLog(String, String, ActionStatus)}
     */
    @Override
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), new ConsentActionRequest(tppId, consentId, actionStatus), Void.class);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForSinglePaymentAndGetId(SpiPisConsentRequest)}
     */
    @Override
    public String createPisConsentForSinglePaymentAndGetId(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest cmsPisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForSinglePayment(spiPisConsentRequest);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), cmsPisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForBulkPaymentAndGetId(SpiPisConsentRequest)}
     */
    @Override
    public String createPisConsentForBulkPaymentAndGetId(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForBulkPayment(spiPisConsentRequest);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * For detailed description see {@link ConsentSpi#createPisConsentForPeriodicPaymentAndGetId(SpiPisConsentRequest)}
     */
    @Override
    public String createPisConsentForPeriodicPaymentAndGetId(SpiPisConsentRequest spiPisConsentRequest) {
        PisConsentRequest pisConsentRequest = pisConsentMapper.mapToCmsPisConsentRequestForPeriodicPayment(spiPisConsentRequest);
        CreatePisConsentResponse createPisConsentResponse = consentRestTemplate.postForEntity(remotePisConsentUrls.createPisConsent(), pisConsentRequest, CreatePisConsentResponse.class).getBody();

        return Optional.ofNullable(createPisConsentResponse)
                   .map(CreatePisConsentResponse::getConsentId)
                   .orElse(null);
    }


    private boolean isDirectAccessRequest(SpiCreateAisConsentRequest spiCreateAisConsentRequest) {
        SpiAccountAccess spiAccountAccess = spiCreateAisConsentRequest.getAccess();
        return CollectionUtils.isNotEmpty(spiAccountAccess.getBalances())
                   || CollectionUtils.isNotEmpty(spiAccountAccess.getAccounts())
                   || CollectionUtils.isNotEmpty(spiAccountAccess.getTransactions());
    }

    private boolean isInvalidSpiAccountAccessRequest(SpiAccountAccess requestedAccess) {
        Set<String> ibansFromAccess = getIbansFromAccess(requestedAccess);
        List<SpiAccountDetails> accountDetailsList = accountSpi.readAccountDetailsByIbans(
            ibansFromAccess,
            new AspspConsentData("zzzzzzzzzzzzzz".getBytes())).getPayload();

        return ibansFromAccess.stream()
                   .map(acc -> filter(acc, accountDetailsList))
                   .anyMatch(a -> !a);
    }

    private boolean filter(String iban, List<SpiAccountDetails> accountDetailsList) {
        return accountDetailsList.stream()
                   .map(acc -> acc.getIban().equals(iban))
                   .findAny()
                   .orElse(false);
    }

    private Set<String> getIbansFromAccess(SpiAccountAccess access) {
        return Stream.of(
            getIbansFromAccountReference(access.getAccounts()),
            getIbansFromAccountReference(access.getBalances()),
            getIbansFromAccountReference(access.getTransactions())
        )
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private Set<String> getIbansFromAccountReference(List<SpiAccountReference> references) {
        return Optional.ofNullable(references)
                   .map(list -> list.stream()
                                    .map(SpiAccountReference::getIban)
                                    .collect(Collectors.toSet()))
                   .orElseGet(Collections::emptySet);
    }
}
