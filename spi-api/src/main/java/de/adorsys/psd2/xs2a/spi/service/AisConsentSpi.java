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
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import org.jetbrains.annotations.NotNull;

/**
 * Spi interface to be used for AIS consent initiating and revoking, and authorising process through AuthorisationSpi interface.
 */
public interface AisConsentSpi extends AuthorisationSpi<SpiAccountConsent> {

    /**
     * Initiates AIS consent
     *
     * @param psuData                 SpiPsuData container of authorisation data about PSU
     * @param accountConsent          Account consent
     * @param initialAspspConsentData Encrypted data that is stored in the consent management system
     * @return Return a positive or negative response as part of SpiResponse
     */
    SpiResponse<VoidResponse> initiateAisConsent(@NotNull SpiPsuData psuData, SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData);

    /**
     * Revokes AIS consent
     *
     * @param psuData          SpiPsuData container of authorisation data about PSU
     * @param accountConsent   Account consent
     * @param aspspConsentData Encrypted data that is stored in the consent management system.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    SpiResponse<VoidResponse> revokeAisConsent(@NotNull SpiPsuData psuData, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful returns success response. Used only with embedded SCA Approach.
     *
     * @param psuData            SpiPsuData container of authorisation data about PSU
     * @param spiScaConfirmation payment confirmation information
     * @param accountConsent     Account consent
     * @param aspspConsentData   Encrypted data that is stored in the consent management system.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    @NotNull
    SpiResponse<VoidResponse> verifyScaAuthorisation(@NotNull SpiPsuData psuData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData);
}
