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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
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

    // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1627
    void writeErrorLog(AuthorisationProcessorRequest request, PsuIdData psuData, ErrorHolder errorHolder, String message) {
        String businessObjectName = resolveBusinessObjectName(request.getServiceType());
        String messageToLog = String.format("%s [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s Error msg: [{}]", businessObjectName, message);
        log.info(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach(),
                 errorHolder);
    }

    // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1627
    void writeInfoLog(AuthorisationProcessorRequest request, PsuIdData psuData, String message) {
        String businessObjectName = resolveBusinessObjectName(request.getServiceType());
        String messageToLog = String.format("%s [{}], Authorisation-ID [{}], PSU-ID [{}], SCA Approach [{}]. %s", businessObjectName, message);
        log.info(messageToLog,
                 request.getUpdateAuthorisationRequest().getBusinessObjectId(),
                 request.getUpdateAuthorisationRequest().getAuthorisationId(),
                 psuData != null ? psuData.getPsuId() : "-",
                 request.getScaApproach());
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

    protected String resolveBusinessObjectName(ServiceType serviceType) {
        return serviceType == ServiceType.AIS || serviceType == ServiceType.PIIS ? "Consent-ID" : "Payment-ID";
    }
}
