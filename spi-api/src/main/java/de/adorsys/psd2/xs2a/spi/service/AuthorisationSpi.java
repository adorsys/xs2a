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

package de.adorsys.psd2.xs2a.spi.service;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
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
     * Starts SCA authorisation.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param scaApproach              SCA approach from xs2a
     * @param scaStatus                scaStatus from CMS which is always be equal STARTED
     * @param authorisationId          a unique identifier of authorisation process
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns an object containing scaApproach, scaStatus, psuMessage, tppMessages from the bank.
     */
    default SpiResponse<SpiStartAuthorisationResponse> startAuthorisation(@NotNull SpiContextData contextData, @NotNull ScaApproach scaApproach,
                                                                          @NotNull ScaStatus scaStatus, @NotNull String authorisationId,
                                                                          T businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiStartAuthorisationResponse>builder()
                   .payload(new SpiStartAuthorisationResponse(scaApproach, scaStatus, null, null))
                   .build();
    }

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
     * @param scaStatus                scaStatus from CMS
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param authorisationId          a unique identifier of authorisation process
     * @param businessObject           generic consent/payment object
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system.
     * @return Returns response object, containing a SCA information from ASPSP
     */
    SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus,
                                                   @NotNull SpiContextData contextData,
                                                   @NotNull String authorisationId,
                                                   @NotNull T businessObject,
                                                   @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);
}
