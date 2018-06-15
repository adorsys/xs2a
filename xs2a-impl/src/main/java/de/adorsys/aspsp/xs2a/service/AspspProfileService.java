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

import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.config.AspspProfileRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.consent.pis.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j
@Service
@RequiredArgsConstructor
public class AspspProfileService {
    @Qualifier("aspspProfileRestTemplate")
    private final RestTemplate aspspProfileRestTemplate;
    private final AspspProfileRemoteUrls aspspProfileRemoteUrls;

    public List<PaymentProduct> getAvailablePaymentProducts() {
        return Optional.ofNullable(readAvailablePaymentProducts())
                   .map(list -> list.stream()
                                    .map(PaymentProduct::getByCode)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    public List<PaymentType> getAvailablePaymentTypes() {
        return Optional.ofNullable(readAvailablePaymentTypes())
                   .map(list -> list.stream()
                                    .map(PaymentType::getByValue)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    private List<String> readAvailablePaymentProducts() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentProducts(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }
    
   
	public Boolean getTppSignatureRequired() {
		return aspspProfileRestTemplate.exchange(
	            aspspProfileRemoteUrls.getTppSignatureRequired(), HttpMethod.GET, null, Boolean.class).getBody();
	}


    private List<String> readAvailablePaymentTypes() {
        return aspspProfileRestTemplate.exchange(
            aspspProfileRemoteUrls.getAvailablePaymentTypes(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
            }).getBody();
    }
}
