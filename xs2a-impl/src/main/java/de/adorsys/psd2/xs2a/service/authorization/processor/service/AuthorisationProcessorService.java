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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;

/**
 * Implementations of this interface contain the business logic, needed to perform embedded and decoupled SCA
 */
public interface AuthorisationProcessorService {

    /**
     * Updates authorisation in the CMS after each successful authorisation step execution
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @param response {@link AuthorisationProcessorResponse} the response object
     */
    void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response);

    /**
     * Contains business logic to perform at the `received` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `psuIdentified` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `psuAuthenticated` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `scaMethodSelected` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `started` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `finalised` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `failed` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaFailed(AuthorisationProcessorRequest request);

    /**
     * Contains business logic to perform at the `exempted` status of authorisation
     *
     * @param request the request object, containing incoming data from controller and authorisation data from CMS
     * @return the result object, containing the successful result of authorisation or the error response
     */
    AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest request);
}
