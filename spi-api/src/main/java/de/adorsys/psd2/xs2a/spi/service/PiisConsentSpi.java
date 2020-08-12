/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiatePiisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import org.jetbrains.annotations.NotNull;

/**
 * SPI interface to be used for PIIS consent initiating and revoking, and authorising process through AuthorisationSpi interface.
 */
public interface PiisConsentSpi extends AuthorisationSpi<SpiPiisConsent> {

    /**
     * Initiates PIIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           PIIS consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns account access with Account's reference IDs. If Accounts/ReferenceIDs not know, returns empty Response object
     */
    default SpiResponse<SpiInitiatePiisConsentResponse> initiatePiisConsent(@NotNull SpiContextData contextData, SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * This call is invoked inside of Get Consent request to give the bank ability to provide consent status, if it was not saved in CMS before.
     * If consent status is already finalised one, this call will be not performed.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           Account consent from CMS
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Consent Status to be saved in CMS and provided back to TPP, PSU message if added by ASPSP.
     */
    default SpiResponse<SpiConsentStatusResponse> getConsentStatus(@NotNull SpiContextData contextData, @NotNull SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * Revokes PIIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           PIIS consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return void response
     */
    default SpiResponse<VoidResponse> revokePiisConsent(@NotNull SpiContextData contextData, SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful returns success response. Used only with embedded SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiScaConfirmation       payment confirmation information
     * @param spiPiisConsent           Piis consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    default SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * Notify ASPSP about validation result of confirmation code on XS2A side
     *
     * @param contextData                      holder of call's context data (e.g. about PSU and TPP)
     * @param confirmationCodeValidationResult validation result of confirmation code on XS2A side
     * @param piisConsent                      piis consent
     * @param aspspConsentDataProvider         Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains SCA status of authorisation and Consent status
     */
    @NotNull
    default SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull SpiPiisConsent piisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    /**
     * Checks confirmation data at the ASPSP side in case of XS2A not supporting validation of this data. Used only with redirect SCA Approach.
     *
     * @param contextData                     holder of call's context data (e.g. about PSU and TPP)
     * @param spiCheckConfirmationCodeRequest object with confirmation code and authorisation ID
     * @param aspspConsentDataProvider        Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains checking result - if the confirmation data was correct or not
     */
    @NotNull
    default SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    default SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull SpiPsuData psuLoginData, String password, SpiPiisConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    default  @NotNull SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiPiisConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    default  @NotNull SpiResponse<Boolean> requestTrustedBeneficiaryFlag(@NotNull SpiContextData contextData, @NotNull SpiPiisConsent businessObject, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }

    @Override
    default SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiPiisConsent businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        throw new UnsupportedOperationException("Unsupported operation");
    }
}
