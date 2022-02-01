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

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
abstract class BaseAuthorisationProcessorService implements AuthorisationProcessorService {
    private static final String UNSUPPORTED_ERROR_MESSAGE = "Current SCA status is not supported";

    @Override
    public AuthorisationProcessorResponse doScaStarted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaFailed(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    @Override
    public AuthorisationProcessorResponse doScaExempted(AuthorisationProcessorRequest authorisationProcessorRequest) {
        throw new UnsupportedOperationException(UNSUPPORTED_ERROR_MESSAGE);
    }

    boolean isPsuExist(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(PsuIdData::isNotEmpty)
                   .orElse(false);
    }

    boolean isSingleScaMethod(List<AuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() == 1;
    }

    boolean isMultipleScaMethods(List<AuthenticationObject> spiScaMethods) {
        return spiScaMethods.size() > 1;
    }

    ChallengeData mapToChallengeData(SpiAuthorizationCodeResult authorizationCodeResult) {
        if (authorizationCodeResult == null || authorizationCodeResult.isEmpty()) {
            return null;
        }
        return authorizationCodeResult.getChallengeData();
    }

    PsuIdData extractPsuIdData(CommonAuthorisationParameters request,
                               Authorisation authorisation) {
        PsuIdData psuDataInRequest = request.getPsuData();
        return isPsuExist(psuDataInRequest) ? psuDataInRequest : authorisation.getPsuIdData();
    }
}
