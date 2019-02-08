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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface, that contains the method for the authorisation flow.
 * To be used in SPI interfaces, that need authorisation functionality.
 *
 * @param <T> business object to be provided during the implementation
 */
interface AuthorisationSpi<T> {

    /**
     * Authorises psu and returns current authorisation status. Used only with embedded SCA Approach.
     *
     * @param contextData      holder of call's context data (e.g. about PSU and TPP)
     * @param psuLoginData     ASPSP identifier(s) of the psu, provided by TPP within this request.
     * @param password         Psu's password
     * @param businessObject   generic consent/payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return success or failure authorization status
     */
    SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiContextData contextData, @NotNull SpiPsuData psuLoginData, String password, T businessObject, @NotNull AspspConsentData aspspConsentData);

    /**
     * Returns a list of SCA methods for PSU by its login. Used only with embedded SCA Approach.
     *
     * @param contextData      holder of call's context data (e.g. about PSU and TPP)
     * @param businessObject   generic consent/payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of SCA methods applicable for specified PSU
     */
    SpiResponse<List<SpiAuthenticationObject>> requestAvailableScaMethods(@NotNull SpiContextData contextData, T businessObject, @NotNull AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorisation depending on selected SCA method. Used only with embedded SCA Approach.
     *
     * @param contextData            holder of call's context data (e.g. about PSU and TPP)
     * @param authenticationMethodId Id of a chosen sca method
     * @param businessObject         generic consent/payment object
     * @param aspspConsentData       Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                               May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse. If authentication method is unknown, then empty SpiAuthorizationCodeResult should be returned.
     */
    @NotNull
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull T businessObject, @NotNull AspspConsentData aspspConsentData);

    /**
     * Notifies a decoupled app about starting SCA. AuthorisationId is provided to allow the app to access CMS(the same way like redirectId is used in Redirect Approach). Used only with decoupled SCA Approach.
     *
     * @param contextData            holder of call's context data (e.g. about PSU and TPP)
     * @param authorisationId        a unique identifier of authorisation process
     * @param authenticationMethodId Id of a chosen sca method(for a decoupled SCA method within embedded approach)
     * @param businessObject         generic consent/payment object
     * @param aspspConsentData       Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                               May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a response object, containing a message from ASPSP to PSU, giving him instructions regarding decoupled SCA starting.
     */
    @NotNull
    default SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull String authenticationMethodId, @NotNull T businessObject, @NotNull AspspConsentData aspspConsentData) {
        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder().fail(SpiResponseStatus.NOT_SUPPORTED);
    }
}
