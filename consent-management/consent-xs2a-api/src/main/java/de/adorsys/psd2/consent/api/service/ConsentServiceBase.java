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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

import java.util.List;

/**
 * Base version of ConsentService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see ConsentService
 * @see ConsentServiceEncrypted
 */
interface ConsentServiceBase {

    /**
     * Create AIS consent
     *
     * @param consent needed parameters for creating AIS consent
     * @return create consent response, containing consent and its encrypted ID
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    CmsResponse<CmsCreateConsentResponse> createConsent(CmsConsent consent) throws WrongChecksumException;

    /**
     * Reads status of consent by id
     *
     * @param consentId id of consent
     * @return ConsentStatus
     */
    CmsResponse<ConsentStatus> getConsentStatusById(String consentId);

    /**
     * Updates consent status by id
     *
     * @param consentId id of consent
     * @param status    new consent status
     * @return true if consent was found and status was updated, false otherwise.
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    CmsResponse<Boolean> updateConsentStatusById(String consentId, ConsentStatus status) throws WrongChecksumException;

    /**
     * Reads full information of consent by id
     *
     * @param consentId id of consent
     * @return AisAccountConsent
     */
    CmsResponse<CmsConsent> getConsentById(String consentId);

    /**
     * Finds old consents for current TPP and PSU and terminates them.
     * This method should be invoked, when a new consent is authorised.
     *
     * @param newConsentId id of new consent
     * @return true if any consents have been terminated, false - if none
     */
    CmsResponse<Boolean> findAndTerminateOldConsentsByNewConsentId(String newConsentId);

    /**
     * Finds old consents for current TPP and PSU and terminates them.
     * This method should be invoked, when a new consent is authorised.
     *
     * @param newConsentId id of new consent
     * @param request      terminate old consent request
     * @return true if any consents have been terminated, false - if none
     */
    CmsResponse<Boolean> findAndTerminateOldConsents(String newConsentId, TerminateOldConsentsRequest request);

    CmsResponse<List<PsuIdData>> getPsuDataByConsentId(String consentId);

    /**
     * Updates multilevel SCA required field
     *
     * @param consentId             String representation of the consent identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     * @return <code>true</code> if authorisation was found and SCA required field updated, <code>false</code> otherwise
     * @throws WrongChecksumException in case of any attempt to change definite consent fields after its status became valid.
     */
    CmsResponse<Boolean> updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) throws WrongChecksumException;
}
