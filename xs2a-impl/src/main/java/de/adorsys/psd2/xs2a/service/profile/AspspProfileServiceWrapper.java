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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.config.cache.CacheConfig;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AspspProfileServiceWrapper {
    private final AspspProfileService aspspProfileService;

    /**
     * Gets a list of payment products allowed by current ASPSP from ASPSP profile service
     *
     * @return List of payment products supported by current ASPSP
     */
    public List<PaymentProduct> getAvailablePaymentProducts() {
        List<PaymentProduct> availablePaymentProducts = readAspspSettings().getAvailablePaymentProducts();
        return Optional.ofNullable(availablePaymentProducts)
            .map(Collections::unmodifiableList)
            .orElse(Collections.emptyList());
    }

    /**
     * Gets a list of payment types available at current ASPSP from ASPSP profile service
     *
     * @return List of payment types allowed by ASPSP
     */
    public List<PaymentType> getAvailablePaymentTypes() {
        return readAspspSettings().getAvailablePaymentTypes();
    }

    /**
     * Reads sca approach from ASPSP profile service
     *
     * @return Available SCA approach for tpp
     */
    @Cacheable(CacheConfig.ASPSP_PROFILE_CACHE)
    public ScaApproach getScaApproach() {
        ScaApproach scaApproach = aspspProfileService.getScaApproach();
        return Optional.ofNullable(scaApproach)
            .map(approach -> ScaApproach.valueOf(approach.name()))
            .orElse(ScaApproach.REDIRECT); //default
    }

    /**
     * Reads requirement of tpp signature from ASPSP profile service
     *
     * @return 'true' if tpp signature is required, 'false' if not
     */
    public Boolean getTppSignatureRequired() {
        return readAspspSettings().isTppSignatureRequired();
    }

    /**
     * Reads get PIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getPisRedirectUrlToAspsp() {
        return readAspspSettings().getPisRedirectUrlToAspsp();
    }

    /**
     * Reads get AIS redirect url to aspsp from ASPSP profile service
     *
     * @return Url in order to redirect SCA approach
     */
    public String getAisRedirectUrlToAspsp() {
        return readAspspSettings().getAisRedirectUrlToAspsp();
    }

    /**
     * Retrieves list of supported Xs2aAccountReference fields from ASPSP profile service
     *
     * @return List of supported fields
     */
    public List<SupportedAccountReferenceField> getSupportedAccountReferenceFields() {
        List<de.adorsys.psd2.aspsp.profile.domain.SupportedAccountReferenceField> supportedAccountReferenceFields = readAspspSettings().getSupportedAccountReferenceFields();
        return supportedAccountReferenceFields.stream()
            .map(reference -> SupportedAccountReferenceField.valueOf(reference.name()))
            .collect(Collectors.toList());
    }

    /**
     * Reads value of maximum consent lifetime
     *
     * @return int value of maximum consent lifetime
     */
    public int getConsentLifetime() {
        return readAspspSettings().getConsentLifetime();
    }

    /**
     * Reads value of AllPsd2Support from ASPSP profile service
     *
     * @return true if ASPSP supports Global consents, false if doesn't
     */
    public Boolean getAllPsd2Support() {
        return readAspspSettings().isAllPsd2Support();
    }

    /**
     * Reads value BankOfferedConsentSupported
     *
     * @return boolean representation of support of Bank Offered Consent
     */
    public boolean isBankOfferedConsentSupported() {
        return readAspspSettings().isBankOfferedConsentSupport();
    }

    /**
     * Reads value of transactions without balances supported from ASPSP profile service
     *
     * @return true if ASPSP transactions without balances supported, false if doesn't
     */
    public boolean isTransactionsWithoutBalancesSupported() {
        return readAspspSettings().isTransactionsWithoutBalancesSupported();
    }

    /**
     * Reads if signing basket supported from ASPSP profile service
     *
     * @return true if ASPSP supports signing basket , false if doesn't
     */
    public boolean isSigningBasketSupported() {
        return readAspspSettings().isSigningBasketSupported();
    }

    /**
     * Reads if is payment cancellation authorization mandated from ASPSP profile service
     *
     * @return true if payment cancellation authorization is mandated, false if doesn't
     */
    public boolean isPaymentCancellationAuthorizationMandated() {
        return readAspspSettings().isPaymentCancellationAuthorizationMandated();
    }

    public boolean isPiisConsentSupported() {
        return readAspspSettings().isPiisConsentSupported();
    }

    private AspspSettings readAspspSettings() {
        return aspspProfileService.getAspspSettings();
    }
}
