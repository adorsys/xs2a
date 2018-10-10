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

package de.adorsys.aspsp.xs2a.spi.service.v2;

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;

import java.util.List;

/**
 * Interface, that contains the method for the authorisation flow.
 * To be used in SPI interfaces, that need authorisation functionality.
 *
 * @param <T> business object to be provided during the implementation
 */
interface AuthorisationSpi<T> {

    /**
     * Authorises psu and returns current autorisation status. Used only with embedded SCA Approach.
     *
     * @param psuId            ASPSP identifier of the psu
     * @param password         Psu's password
     * @param businessObject   generic payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return success or failure authorization status
     */
    SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, T businessObject, AspspConsentData aspspConsentData);

    /**
     * Returns a list of SCA methods for PSU by its login. Used only with embedded SCA Approach.
     *
     * @param psuId            ASPSP identifier of the psu
     * @param businessObject   generic payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of SCA methods applicable for specified PSU
     */
    SpiResponse<List<SpiScaMethod>> requestAvailableScaMethods(String psuId, T businessObject, AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorisation depending on selected SCA method. Used only with embedded SCA Approach.
     *
     * @param psuId            ASPSP identifier of the psu
     * @param scaMethod        Chosen sca method
     * @param businessObject   generic payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(String psuId, SpiScaMethod scaMethod, T businessObject, AspspConsentData aspspConsentData);

    /**
     * Sends authorisation confirmation information (secure code or such) to ASPSP and if case of successful validation executes payment at ASPSP. Used only with embedded SCA Approach.
     *
     * @param spiScaConfirmation payment confirmation information
     * @param businessObject     generic payment object
     * @param aspspConsentData   Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                           May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Return a positive or negative response as part of SpiResponse
     */
    SpiResponse verifyAuthorisationCodeAndExecuteRequest(SpiScaConfirmation spiScaConfirmation, T businessObject, AspspConsentData aspspConsentData);
}
