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

import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.ObjectHolder;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.PaymentSpi;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAspspAuthorisationData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;

@Component
@AllArgsConstructor
public class AccountSpiImpl implements AccountSpi {
    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final KeycloakInvokerService keycloakInvokerService;
    private final JsonConverter jsonConverter;

    /**
     * For detailed description see {@link AccountSpi#readAccountDetailsByIban(String, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIban(String iban, AspspConsentData aspspConsentData) {
        List<SpiAccountDetails> response = Optional.ofNullable(
            aspspRestTemplate.exchange(
                remoteSpiUrls.getAccountDetailsByIban(),
                HttpMethod.GET,
                new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
                }, iban)
                .getBody()
        )
                                               .orElseGet(Collections::emptyList);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readTransactionsByPeriod(String, LocalDate, LocalDate, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiTransaction>> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo, AspspConsentData aspspConsentData) {
        Map<String, String> uriParams = new ObjectHolder<String, String>()
                                            .addValue("account-id", accountId)
                                            .getValues();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remoteSpiUrls.readTransactionsByPeriod())
                                           .queryParam("dateFrom", dateFrom)
                                           .queryParam("dateTo", dateTo);

        Optional<List<SpiTransaction>> response = Optional.ofNullable(aspspRestTemplate.exchange(
            builder.buildAndExpand(uriParams).toUriString(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiTransaction>>() {
            }).getBody());
        return new SpiResponse<>(response.orElseGet(ArrayList::new), aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readTransactionById(String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<Optional<SpiTransaction>> readTransactionById(String transactionId, String accountId, AspspConsentData aspspConsentData) {
        Optional<SpiTransaction> response = Optional.ofNullable(aspspRestTemplate.getForObject(remoteSpiUrls.readTransactionById(), SpiTransaction.class, transactionId, accountId));
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountDetails(String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiAccountDetails> readAccountDetails(String accountId, AspspConsentData aspspConsentData) {
        SpiAccountDetails response = aspspRestTemplate.getForObject(remoteSpiUrls.getAccountDetailsById(), SpiAccountDetails.class, accountId);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountsByPsuId(String, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiAccountDetails>> readAccountsByPsuId(String psuId, AspspConsentData aspspConsentData) {
        List<SpiAccountDetails> response = Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            }, psuId).getBody())
                                               .orElseGet(Collections::emptyList);
        return new SpiResponse<>(response, aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readAccountDetailsByIbans(Collection, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIbans(Collection<String> ibans, AspspConsentData aspspConsentData) {
        List<SpiAccountDetails> accountDetails = new ArrayList<>();
        for (String iban : ibans) {
            List<SpiAccountDetails> det = readAccountDetailsByIban(iban, aspspConsentData).getPayload();
            if (CollectionUtils.isEmpty(det)) {
                return new SpiResponse<>(Collections.emptyList(), aspspConsentData);
            }
            accountDetails.addAll(det);
        }

        return new SpiResponse<>(accountDetails, aspspConsentData);
    }

    /**
     * For detailed description see {@link AccountSpi#readPsuAllowedPaymentProductList(SpiAccountReference, AspspConsentData)}
     */
    @Override
    public SpiResponse<List<String>> readPsuAllowedPaymentProductList(SpiAccountReference reference, AspspConsentData aspspConsentData) {
        List<String> response = Optional.ofNullable(aspspRestTemplate.exchange(remoteSpiUrls.getAllowedPaymentProducts(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
        }, reference.getIban()).getBody())
                                    .orElseGet(Collections::emptyList);
        return new SpiResponse<>(response, aspspConsentData);
    }

    @Override
    public SpiResponse<List<SpiScaMethod>> readAvailableScaMethods(String psuId, String password, AspspConsentData aspspConsentData) {
        ResponseEntity<List<SpiScaMethod>> response = aspspRestTemplate.exchange(
            remoteSpiUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiScaMethod>>() {
            }, psuId);
        List<SpiScaMethod> spiScaMethods = Optional.ofNullable(response.getBody())
                                               .orElseGet(Collections::emptyList);
        return new SpiResponse<>(spiScaMethods, aspspConsentData);
    }

    /**
     * For detailed description see {@link PaymentSpi#authorisePsu(String, String, AspspConsentData)}
     */
    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData) {
        Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(psuId, password);
        SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                            .orElse(FAILURE);
        byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                             .map(String::getBytes)
                             .orElse(null);
        return new SpiResponse<>(spiAuthorisationStatus, aspspConsentData.respondWith(payload));
    }

    /**
     * For detailed description see {@link PaymentSpi#performStrongUserAuthorisation(String, SpiScaMethod, AspspConsentData)}
     */
    @Override
    public void performStrongUserAuthorisation(String psuId, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(remoteSpiUrls.getGenerateTanConfirmationForAis(), HttpMethod.POST, null, Void.class, psuId);
    }

    @Override
    public void applyStrongUserAuthorisation(SpiAccountConfirmation confirmation, AspspConsentData aspspConsentData) {
        aspspRestTemplate.exchange(remoteSpiUrls.applyStrongUserAuthorisationForAis(), HttpMethod.PUT, new HttpEntity<>(confirmation), ResponseEntity.class);
    }
}
