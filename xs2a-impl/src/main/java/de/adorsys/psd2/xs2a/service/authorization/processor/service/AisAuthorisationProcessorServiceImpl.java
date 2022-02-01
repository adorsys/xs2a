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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.TerminateOldConsentsRequest;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AbstractAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiStartAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<AisConsent> {
    private final List<AbstractAisAuthorizationService> services;
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final AisScaAuthorisationService aisScaAuthorisationService;

    public AisAuthorisationProcessorServiceImpl(Xs2aAuthorisationService authorisationService,
                                                SpiContextDataProvider spiContextDataProvider,
                                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                SpiErrorMapper spiErrorMapper,
                                                Xs2aToSpiPsuDataMapper psuDataMapper,
                                                List<AbstractAisAuthorizationService> services,
                                                Xs2aAisConsentService aisConsentService,
                                                AisConsentSpi aisConsentSpi,
                                                Xs2aAisConsentMapper aisConsentMapper,
                                                CommonDecoupledAisService commonDecoupledAisService,
                                                AisScaAuthorisationService aisScaAuthorisationService) {
        super(authorisationService, spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, psuDataMapper);
        this.services = services;
        this.aisConsentService = aisConsentService;
        this.aisConsentSpi = aisConsentSpi;
        this.aisConsentMapper = aisConsentMapper;
        this.commonDecoupledAisService = commonDecoupledAisService;
        this.aisScaAuthorisationService = aisScaAuthorisationService;
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        ConsentAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    private ConsentAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("Ais authorisation service was not found for approach " + scaApproach));
    }

    @Override
    ErrorType getErrorType400() {
        return ErrorType.AIS_400;
    }

    @Override
    ErrorType getErrorType401() {
        return ErrorType.AIS_401;
    }

    @Override
    void findAndTerminateOldConsents(String consentId, AisConsent consent) {
        var request = new TerminateOldConsentsRequest(consent.isOneAccessType(),
                                                      consent.isWrongConsentData(),
                                                      consent.getPsuIdDataList(),
                                                      Optional.ofNullable(consent.getTppInfo())
                                                          .map(TppInfo::getAuthorisationNumber)
                                                          .orElse(null),
                                                      consent.getInstanceId());
        aisConsentService.findAndTerminateOldConsents(consentId, request);
    }

    @Override
    void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus) {
        aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
    }

    @Override
    void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        aisConsentService.updateMultilevelScaRequired(consentId, true);
    }

    @Override
    ServiceType getServiceType() {
        return ServiceType.AIS;
    }

    @Override
    protected SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData, ScaApproach scaApproach, ScaStatus scaStatus, String authorisationId, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.startAuthorisation(spiContextData, scaApproach, scaStatus, authorisationId, aisConsentMapper.mapToSpiAccountConsent(consent), spiAspspConsentDataProvider);
    }

    @Override
    UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, AisConsent consent, String authenticationMethodId, PsuIdData psuData) {
        return commonDecoupledAisService.proceedDecoupledApproach(consentId, authorisationId,
                                                                  aisConsentMapper.mapToSpiAccountConsent(consent),
                                                                  authenticationMethodId, psuData);
    }

    @Override
    Optional<AisConsent> getConsentByIdFromCms(String consentId) {
        return aisConsentService.getAccountConsentById(consentId);
    }

    @Override
    SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData provideWithPsuIdData, String authenticationMethodId, AisConsent consent, SpiAspspConsentDataProvider spiAspspDataProviderFor) {
        return aisConsentSpi.requestAuthorisationCode(provideWithPsuIdData,
                                                      authenticationMethodId,
                                                      aisConsentMapper.mapToSpiAccountConsent(consent),
                                                      spiAspspDataProviderFor);

    }

    @Override
    boolean isOneFactorAuthorisation(AisConsent consent) {
        return aisScaAuthorisationService.isOneFactorAuthorisation(consent);
    }

    @Override
    SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.authorisePsu(spiContextData,
                                          authorisationId,
                                          spiPsuData,
                                          password,
                                          aisConsentMapper.mapToSpiAccountConsent(consent),
                                          spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.requestAvailableScaMethods(spiContextData,
                                                        aisConsentMapper.mapToSpiAccountConsent(consent),
                                                        spiAspspConsentDataProvider);
    }

    @Override
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, CommonAuthorisationParameters request, PsuIdData psuData, AisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return aisConsentSpi.verifyScaAuthorisation(spiContextData,
                                                    aisConsentMapper.mapToSpiScaConfirmation(request, psuData),
                                                    aisConsentMapper.mapToSpiAccountConsent(consent),
                                                    spiAspspConsentDataProvider);
    }
}
