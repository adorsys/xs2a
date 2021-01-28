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

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PiisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<PiisConsent> {
    private final List<PiisAuthorizationService> services;
    private final Xs2aPiisConsentService piisConsentService;
    private final PiisConsentSpi piisConsentSpi;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final CommonDecoupledPiisService commonDecoupledPiisService;
    private final PiisScaAuthorisationService piisScaAuthorisationService;

    public PiisAuthorisationProcessorServiceImpl(Xs2aAuthorisationService authorisationService,
                                                 SpiContextDataProvider spiContextDataProvider,
                                                 SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                 SpiErrorMapper spiErrorMapper,
                                                 Xs2aToSpiPsuDataMapper psuDataMapper,
                                                 List<PiisAuthorizationService> services,
                                                 Xs2aPiisConsentService piisConsentService,
                                                 PiisConsentSpi piisConsentSpi,
                                                 Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper,
                                                 CommonDecoupledPiisService commonDecoupledPiisService,
                                                 PiisScaAuthorisationService piisScaAuthorisationService) {
        super(authorisationService, spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, psuDataMapper);
        this.services = services;
        this.piisConsentService = piisConsentService;
        this.piisConsentSpi = piisConsentSpi;
        this.xs2aToSpiPiisConsentMapper = xs2aToSpiPiisConsentMapper;
        this.commonDecoupledPiisService = commonDecoupledPiisService;
        this.piisScaAuthorisationService = piisScaAuthorisationService;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        PiisAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    private PiisAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Piis authorisation service was not found for approach " + scaApproach));
    }

    @Override
    ErrorType getErrorType400() {
        return ErrorType.PIIS_400;
    }

    @Override
    ErrorType getErrorType401() {
        return ErrorType.PIIS_401;
    }

    @Override
    void findAndTerminateOldConsentsByNewConsentId(String consentId) {
        // this method is empty because one tpp could have more then one valid piis consent
    }

    @Override
    void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        piisConsentService.updateConsentStatus(consentId, responseConsentStatus);
    }

    @Override
    void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        piisConsentService.updateMultilevelScaRequired(consentId, true);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.PIIS;
    }

    @Override
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, UpdateAuthorisationRequest request, PsuIdData psuData, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.verifyScaAuthorisation(spiContextData,
                                                     xs2aToSpiPiisConsentMapper.toSpiScaConfirmation(request, psuData),
                                                     xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                                     spiAspspConsentDataProvider);
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, PiisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return commonDecoupledPiisService.proceedDecoupledApproach(consentId, authorisationId,
                                                                   xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                                                   authenticationMethodId, psuData);
    }

    @Override
    Optional<PiisConsent> getConsentByIdFromCms(String consentId) {
        return piisConsentService.getPiisConsentById(consentId);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData, String authenticationMethodId, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.requestAuthorisationCode(spiContextData,
                                                       authenticationMethodId,
                                                       xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                                       spiAspspConsentDataProvider);
    }

    @Override
    boolean isOneFactorAuthorisation(PiisConsent consent) {
        return piisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.authorisePsu(spiContextData,
                                           authorisationId,
                                           spiPsuData,
                                           password,
                                           xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                           spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.requestAvailableScaMethods(spiContextData,
                                                         xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                                         spiAspspConsentDataProvider);
    }
}
