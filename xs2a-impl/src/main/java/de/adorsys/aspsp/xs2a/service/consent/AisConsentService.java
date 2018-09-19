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

import de.adorsys.aspsp.xs2a.config.rest.consent.AisConsentRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import de.adorsys.aspsp.xs2a.consent.api.AisConsentStatusResponse;
import de.adorsys.aspsp.xs2a.consent.api.ConsentActionRequest;
import de.adorsys.aspsp.xs2a.consent.api.ais.*;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiUpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
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
public class AisConsentService {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final AccountSpi accountSpi;

    /**
     * Sends a POST request to CMS to store created AISconsent
     *
     * @param request          Request body storing main consent details
     * @param psuId            String representation of PSU`s identifier at ASPSP
     * @param tppId            String representation of TPP`s identifier from TPP Certificate
     * @param aspspConsentData Aspsp private binary data
     * @return String representation of identifier of stored consent
     */
    public String createConsent(CreateConsentReq request, String psuId, String tppId, AspspConsentData aspspConsentData) {
        if (isDirectAccessRequest(request) && isInvalidSpiAccountAccessRequest(request.getAccess())) {
            return null;
        }

        CreateAisConsentRequest createAisConsentRequest = aisConsentMapper.mapToCreateAisConsentRequest(request, psuId, tppId, aspspConsentData);
        CreateAisConsentResponse createAisConsentResponse = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsent(), createAisConsentRequest, CreateAisConsentResponse.class).getBody();

        return Optional.ofNullable(createAisConsentResponse)
                   .map(CreateAisConsentResponse::getConsentId)
                   .orElse(null);
    }

    /**
     * Requests CMS to retrieve AIS consent by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent
     */
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentById(), SpiAccountConsent.class, consentId).getBody();
    }

    /**
     * Requests CMS to retrieve AIS consent status by its identifier
     *
     * @param consentId String representation of identifier of stored consent
     * @return Response containing AIS Consent Status
     */
    public SpiConsentStatus getAccountConsentStatusById(String consentId) {
        AisConsentStatusResponse response = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentStatusById(), AisConsentStatusResponse.class, consentId).getBody();
        return aisConsentMapper.mapToSpiConsentStatus(response.getConsentStatus())
                   .orElse(null);
    }

    /**
     * Requests CMS to update consent status to "Revoked by PSU" state
     *
     * @param consentId String representation of identifier of stored consent
     */
    public void revokeConsent(String consentId) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentStatus(), null, consentId, SpiConsentStatus.REVOKED_BY_PSU);
    }

    /**
     * Sends a POST request to CMS to perform decrement of consent usages and report status of the operation held with certain AIS consent
     *
     * @param tppId        String representation of TPP`s identifier from TPP Certificate
     * @param consentId    String representation of identifier of stored consent
     * @param actionStatus Enum value representing whether the acition is successful or errors occured
     */
    public void consentActionLog(String tppId, String consentId, ActionStatus actionStatus) {
        consentRestTemplate.postForEntity(remoteAisConsentUrls.consentActionLog(), new ConsentActionRequest(tppId, consentId, actionStatus), Void.class);
    }

    /**
     * Sends a POST request to CMS to store created consent authorization
     *
     * @param consentId String representation of identifier of stored consent
     * @return long representation of identifier of stored consent authorization
     */
    public Optional<String> createAisConsentAuthorization(String consentId, SpiScaStatus scaStatus) {
        AisConsentAuthorizationRequest request = aisConsentMapper.mapToAisConsentAuthorization(scaStatus);

        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
            request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
                   .map(CreateAisConsentAuthorizationResponse::getAuthorizationId);
    }

    /**
     * Requests CMS to retrieve AIS consent authorization by its identifier
     *
     * @param authorizationId String representation of identifier of stored consent authorization
     * @return Response containing AIS Consent Authorization
     */

    public SpiAccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        AisConsentAuthorizationResponse resp = consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId).getBody();

        return aisConsentMapper.mapToSpiAccountConsentAuthorization(resp);
    }

    /**
     * Sends a PUT request to CMS to update created AIS consent authorization
     *
     * @param updatePsuData Consent psu data
     */
    public void updateConsentAuthorization(SpiUpdateConsentPsuDataReq updatePsuData) {
        final String consentId = updatePsuData.getConsentId();
        final String authorizationId = updatePsuData.getAuthorizationId();
        final AisConsentAuthorizationRequest request = aisConsentMapper.mapToAisConsentAuthorizationRequest(updatePsuData);

        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, consentId, authorizationId);
    }

    private boolean isDirectAccessRequest(CreateConsentReq request) {
        Xs2aAccountAccess accountAccess = request.getAccess();
        return CollectionUtils.isNotEmpty(accountAccess.getBalances())
                   || CollectionUtils.isNotEmpty(accountAccess.getAccounts())
                   || CollectionUtils.isNotEmpty(accountAccess.getTransactions());
    }

    private boolean isInvalidSpiAccountAccessRequest(Xs2aAccountAccess requestedAccess) {
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

    private Set<String> getIbansFromAccess(Xs2aAccountAccess access) {
        return Stream.of(
            getIbansFromAccountReference(access.getAccounts()),
            getIbansFromAccountReference(access.getBalances()),
            getIbansFromAccountReference(access.getTransactions())
        )
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    private Set<String> getIbansFromAccountReference(List<AccountReference> references) {
        return Optional.ofNullable(references)
                   .map(list -> list.stream()
                                    .map(AccountReference::getIban)
                                    .collect(Collectors.toSet()))
                   .orElseGet(Collections::emptySet);
    }
}
