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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param authorisationId          a unique identifier of authorisation process
     * @param psuLoginData             ASPSP identifier(s) of the psu, provided by TPP within this request.
     * @param password                 Psu's password
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns an object, containing the status of the authorisation and an indicator whether the SCA should be exempted
     */
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull SpiPsuData psuLoginData, String password, T businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Returns a list of SCA methods for PSU by its login. Used only with embedded SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return a list of SCA methods applicable for specified PSU
     */
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, T businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Performs strong customer authorisation depending on selected SCA method. Used only with embedded SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param authenticationMethodId   Id of a chosen sca method
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns an object, containing selected SCA method and challenge data
     */
    @NotNull
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull T businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Notifies a decoupled app about starting SCA. AuthorisationId is provided to allow the app to access CMS(the same way like redirectId is used in Redirect Approach). Used only with decoupled SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param authorisationId          a unique identifier of authorisation process
     * @param authenticationMethodId   Id of a chosen sca method(for a decoupled SCA method within embedded approach)
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns a response object, containing a message from ASPSP to PSU, giving him instructions regarding decoupled SCA starting.
     */
    @NotNull
    default SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull T businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .error(new TppMessage(MessageErrorCode.SERVICE_NOT_SUPPORTED))
                   .build();
    }

    /**
     * Returns authorisation SCA status and PSU message
     *
     * @param authorisationId a unique identifier of authorisation process
     * @return Returns response object, containing a SCA information from ASPSP
     */
    @Deprecated
    //TODO: remove deprecated method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1507
    SpiResponse<SpiScaInformationResponse> getScaInformation(@NotNull SpiContextData contextData,
                                                             @NotNull String authorisationId,
                                                             @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Returns authorisation SCA status and PSU message
     *
     * @param scaStatus                scaStatus from CMS
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param authorisationId          a unique identifier of authorisation process
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns response object, containing a SCA information from ASPSP
     */
    //TODO: remove default implementation with getScaInformation https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1507
    default SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus,
                                                           @NotNull SpiContextData contextData,
                                                           @NotNull String authorisationId,
                                                           @NotNull T businessObject,
                                                           @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        SpiResponse<SpiScaInformationResponse> scaInformation = getScaInformation(contextData, authorisationId, aspspConsentDataProvider);
        if (scaInformation.hasError()) {
            return SpiResponse.<SpiScaStatusResponse>builder()
                       .error(scaInformation.getErrors())
                       .build();
        }
        return SpiResponse.<SpiScaStatusResponse>builder()
                   .payload(new SpiScaStatusResponse(scaStatus, false, null))
                   .build();
    }
}
