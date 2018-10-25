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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;


public interface CmsPsuAisService {
    /**
     * Updates PSU Data in consent, based on the trusted information about PSU known to ASPSP (i.e. after authorisation)
     *
     * @param psuIdData PSU Data to put. If some fields are nullable, the existing values will be overwritten.
     * @param consentId External ID of Consent known to TPP and ASPSP
     * @return <code>true</code> if consent was found and data was updated. <code>false</code> otherwise.
     */
    boolean updatePsuDataInConsent(PsuIdData psuIdData, String consentId);

    AisConsent getConsent(PsuIdData psuIdData, String consentId);

    boolean updateAuthorisationStatus(PsuIdData psuIdData, String consentId, ScaStatus status);

    boolean updateConsentStatus(PsuIdData psuIdData, String consentId, ConsentStatus status);

    List<AisConsent> getConsentsForPsu(PsuIdData psuIdData);

    boolean revokeConsent(PsuIdData psuIdData, String consentId);

    boolean updateAspspConsentData(String consentId, AspspConsentData aspspConsentData);
}
