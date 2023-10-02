/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AbstractAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.*;
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
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaApproach;
import de.adorsys.psd2.xs2a.spi.domain.sca.SpiScaStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_403;

@Service
public class AisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<AisConsent> {
    private final List<AbstractAisAuthorizationService> services;
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;

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
                                                AisScaAuthorisationService aisScaAuthorisationService,
                                                SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper,
                                                SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper,
                                                SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper,
                                                SpiToXs2aConsentStatusMapper spiToXs2aConsentStatusMapper,
                                                SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper,
                                                AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver) {
        super(authorisationService, spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, spiToXs2aScaStatusMapper, spiToXs2aScaApproachMapper, spiToXs2aTppMessageInformationMapper, spiToXs2aConsentStatusMapper, spiToXs2aAuthenticationObjectMapper, spiToXs2aChallengeDataMapper, psuDataMapper);
        this.services = services;
        this.aisConsentService = aisConsentService;
        this.aisConsentSpi = aisConsentSpi;
        this.aisConsentMapper = aisConsentMapper;
        this.commonDecoupledAisService = commonDecoupledAisService;
        this.aisScaAuthorisationService = aisScaAuthorisationService;
        this.aisScaAuthorisationServiceResolver = aisScaAuthorisationServiceResolver;
    }

    @Override
    protected AuthorisationProcessorResponse processAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest,
                                                               SpiStartAuthorisationResponse startAuthorisationResponse,
                                                               ScaStatus scaStatus,
                                                               ScaApproach scaApproach) {
        CommonAuthorisationParameters authParameters = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        PsuIdData psuIdData = authParameters.getPsuData();
        String consentId = authParameters.getBusinessObjectId();
        String authorisationId = authParameters.getAuthorisationId();

        ConsentAuthorizationService service = aisScaAuthorisationServiceResolver.getService();

        Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                        .psuData(psuIdData)
                                                                        .consentId(consentId)
                                                                        .authorisationId(authorisationId)
                                                                        .scaStatus(scaStatus)
                                                                        .scaApproach(scaApproach)
                                                                        .build();

        Optional<CreateConsentAuthorizationResponse> consentAuthorizationResponse = service.createConsentAuthorization(createAuthorisationRequest);

        if (consentAuthorizationResponse.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(AIS_403)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuIdData, errorHolder, AUTHORISATION_FOR_CONSENT_HAS_FAILED);
            return new CreateConsentAuthorisationProcessorResponse(errorHolder, scaApproach, consentId, psuIdData);
        }

        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = consentAuthorizationResponse.get();

        Set<TppMessageInformation> tppMessages = spiToXs2aTppMessageInformationMapper.toTppMessageInformationSet(startAuthorisationResponse.getTppMessages());
        String psuMessage = startAuthorisationResponse.getPsuMessage();

        return new CreateConsentAuthorisationProcessorResponse(createConsentAuthorizationResponse.getScaStatus(),
                                                               createConsentAuthorizationResponse.getScaApproach(),
                                                               psuMessage, tppMessages,
                                                               consentId, authorisationId,
                                                               createConsentAuthorizationResponse.getInternalRequestId(),
                                                               psuIdData);
    }

    @Override
    public void updateAuthorisation(AuthorisationProcessorRequest request, AuthorisationProcessorResponse response) {
        ConsentAuthorizationService authorizationService = getService(request.getScaApproach());
        authorizationService.updateConsentPsuData(request.getUpdateAuthorisationRequest(), response);
    }

    private ConsentAuthorizationService getService(ScaApproach scaApproach) {
        return services.stream().filter(s -> s.getScaApproachServiceType() == scaApproach).findFirst()
                   .orElseThrow(() -> new IllegalArgumentException("AIS authorisation service was not found for approach: " + scaApproach));
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
        return aisConsentSpi.startAuthorisation(spiContextData, SpiScaApproach.valueOf(scaApproach.name()), SpiScaStatus.valueOf(scaStatus.name()), authorisationId, aisConsentMapper.mapToSpiAccountConsent(consent), spiAspspConsentDataProvider);
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
