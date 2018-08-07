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

import de.adorsys.aspsp.xs2a.config.rest.profile.AspspProfileRemoteUrls;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.domain.ScaApproach;
import de.adorsys.aspsp.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AspspProfileService {
    @Qualifier("aspspProfileRestTemplate")
    private final RestTemplate aspspProfileRestTemplate;
    private final AspspProfileRemoteUrls aspspProfileRemoteUrls;

    /**
     * Gets a list of payment products allowed by current ASPSP from ASPSP profile service
     *
     * @return List of payment products supported by current ASPSP
     */
    public List<PaymentProduct> getAvailablePaymentProducts() {
        return Optional.ofNullable(readAvailablePaymentProducts())
                   .map(list -> list.stream()
                                    .map(PaymentProduct::getByCode)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    /**
     * Gets a list of payment types available at current ASPSP from ASPSP profile service
     *
     * @return List of payment types allowed by ASPSP
     */
    public List<PisPaymentType> getAvailablePaymentTypes() {
        return Optional.ofNullable(readAvailablePaymentTypes())
                   .map(list -> list.stream()
                                    .map(PisPaymentType::getByValue)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    /**
     * Reads current sca approach mode from ASPSP profile service
     *
     * @return 'true' if current sca approach requires 'redirect', 'false' if not
     */
    public boolean isRedirectMode() {
        ScaApproach scaApproach = readScaApproach();
        return scaApproach == ScaApproach.REDIRECT
                   || scaApproach == ScaApproach.DECOUPLED;
    }

    private List<String> readAvailablePaymentProducts() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentProducts(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }

    /**
     * Read sca approach from ASPSP profile service
     *
     * @return Available SCA approach for tpp
     */
    public ScaApproach readScaApproach() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getScaApproach(), HttpMethod.GET, null, ScaApproach.class).getBody();
    }

    /**
     * Reads requirement of tpp signature from ASPSP profile service
     *
     * @return 'true' if tpp signature is required, 'false' if not
     */
    public Boolean getTppSignatureRequired() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getTppSignatureRequired(), HttpMethod.GET, null, Boolean.class).getBody();
    }


    /**
     * Read get PIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisRedirectUrlToAspsp() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getPisRedirectUrlToAspsp(), HttpMethod.GET, null, String.class).getBody();
    }

    /**
     * Read get AIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getAisRedirectUrlToAspsp() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAisRedirectUrlToAspsp(), HttpMethod.GET, null, String.class).getBody();
    }

    /**
     * Retrieves list of supported AccountReference fields from ASPSP profile service
     *
     * @return List of supported fields
     */
    public List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getSupportedAccountReferenceFields(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SupportedAccountReferenceField>>() {
            }).getBody();
    }

    private List<String> readAvailablePaymentTypes() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentTypes(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }
}
