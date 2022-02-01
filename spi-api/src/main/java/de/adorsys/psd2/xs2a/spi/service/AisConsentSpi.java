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

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * SPI interface to be used for AIS consent initiating and revoking, and authorising process through AuthorisationSpi interface.
 */
public interface AisConsentSpi extends AuthorisationSpi<SpiAccountConsent> {

    /**
     * Initiates AIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param accountConsent           Account consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns account access with Account's reference IDs. If Accounts/ReferenceIDs not know, returns empty Response object
     */
    SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * This call is invoked inside of Get Consent request to give the bank ability to provide consent status, if it was not saved in CMS before.
     * If consent status is already finalised one, this call will be not performed.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param accountConsent           Account consent from CMS
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Consent Status to be saved in CMS and provided back to TPP, PSU message if added by ASPSP.
     */
    default SpiResponse<SpiConsentStatusResponse> getConsentStatus(@NotNull SpiContextData contextData, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return SpiResponse.<SpiConsentStatusResponse>builder()
                   .payload(new SpiConsentStatusResponse(accountConsent.getConsentStatus(), null))
                   .build();
    }

    /**
     * Revokes AIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param accountConsent           Account consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return void response
     */
    SpiResponse<VoidResponse> revokeAisConsent(@NotNull SpiContextData contextData, SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful returns success response. Used only with embedded SCA Approach.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiScaConfirmation       payment confirmation information
     * @param accountConsent           Account consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Checks confirmation data at the ASPSP side in case of XS2A not supporting validation of this data. Used only with redirect SCA Approach.
     *
     * @param contextData                     holder of call's context data (e.g. about PSU and TPP)
     * @param spiCheckConfirmationCodeRequest object with confirmation code and authorisation ID
     * @param aspspConsentDataProvider        Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains checking result - if the confirmation data was correct or not
     */
    @NotNull
    SpiResponse<SpiConsentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Notify ASPSP about validation result of confirmation code on XS2A side
     *
     * @param contextData                      holder of call's context data (e.g. about PSU and TPP)
     * @param confirmationCodeValidationResult validation result of confirmation code on XS2A side
     * @param accountConsent                   Account consent
     * @param aspspConsentDataProvider         Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns a response object, which contains SCA status of authorisation and Consent status
     */
    @NotNull
    SpiResponse<SpiConsentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull SpiAccountConsent accountConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Checks confirmation data internally in case of XS2A supports validation of this data. Used only with redirect SCA Approach.
     *
     * @param authorisationId          authorisation id
     * @param confirmationCode         confirmation code
     * @param scaAuthenticationData    SCA authentication data
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return confirmation code check result
     */
    default boolean checkConfirmationCodeInternally(String authorisationId, String confirmationCode, String scaAuthenticationData,
                                                    @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return StringUtils.equals(confirmationCode, scaAuthenticationData);
    }
}
