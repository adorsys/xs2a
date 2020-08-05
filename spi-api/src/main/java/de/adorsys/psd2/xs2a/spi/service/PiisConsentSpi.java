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
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiatePiisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import org.jetbrains.annotations.NotNull;

/**
 * SPI interface to be used for PIIS consent initiating and revoking, and authorising process through AuthorisationSpi interface.
 */
public interface PiisConsentSpi {

    /**
     * Initiates PIIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           PIIS consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Returns account access with Account's reference IDs. If Accounts/ReferenceIDs not know, returns empty Response object
     */
    SpiResponse<SpiInitiatePiisConsentResponse> initiatePiisConsent(@NotNull SpiContextData contextData, SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * This call is invoked inside of Get Consent request to give the bank ability to provide consent status, if it was not saved in CMS before.
     * If consent status is already finalised one, this call will be not performed.
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           Account consent from CMS
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return Consent Status to be saved in CMS and provided back to TPP, PSU message if added by ASPSP.
     */
    SpiResponse<SpiConsentStatusResponse> getConsentStatus(@NotNull SpiContextData contextData, @NotNull SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    /**
     * Revokes PIIS consent
     *
     * @param contextData              holder of call's context data (e.g. about PSU and TPP)
     * @param spiPiisConsent           PIIS consent
     * @param aspspConsentDataProvider Provides access to read/write encrypted data to be stored in the consent management system
     * @return void response
     */
    SpiResponse<VoidResponse> revokePiisConsent(@NotNull SpiContextData contextData, SpiPiisConsent spiPiisConsent, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

}
