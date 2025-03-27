/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.ConsentAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.AbstractPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.CommonDecoupledPiisService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
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
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_403;

@Service
public class PiisAuthorisationProcessorServiceImpl extends ConsentAuthorisationProcessorService<PiisConsent> {
    private final List<AbstractPiisAuthorizationService> services;
    private final Xs2aPiisConsentService piisConsentService;
    private final PiisConsentSpi piisConsentSpi;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final CommonDecoupledPiisService commonDecoupledPiisService;
    private final PiisScaAuthorisationService piisScaAuthorisationService;
    private final PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;

    public PiisAuthorisationProcessorServiceImpl(Xs2aAuthorisationService authorisationService,
                                                 SpiContextDataProvider spiContextDataProvider,
                                                 SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                 SpiErrorMapper spiErrorMapper,
                                                 Xs2aToSpiPsuDataMapper psuDataMapper,
                                                 List<AbstractPiisAuthorizationService> services,
                                                 Xs2aPiisConsentService piisConsentService,
                                                 PiisConsentSpi piisConsentSpi,
                                                 Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper,
                                                 CommonDecoupledPiisService commonDecoupledPiisService,
                                                 PiisScaAuthorisationService piisScaAuthorisationService,
                                                 SpiToXs2aScaStatusMapper spiToXs2aScaStatusMapper,
                                                 SpiToXs2aScaApproachMapper spiToXs2aScaApproachMapper,
                                                 SpiToXs2aTppMessageInformationMapper spiToXs2aTppMessageInformationMapper,
                                                 SpiToXs2aConsentStatusMapper spiToXs2aConsentStatusMapper,
                                                 SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                 SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper,
                                                 PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver) {
        super(authorisationService, spiContextDataProvider, aspspConsentDataProviderFactory, spiErrorMapper, spiToXs2aScaStatusMapper, spiToXs2aScaApproachMapper, spiToXs2aTppMessageInformationMapper, spiToXs2aConsentStatusMapper, spiToXs2aAuthenticationObjectMapper, spiToXs2aChallengeDataMapper, psuDataMapper);
        this.services = services;
        this.piisConsentService = piisConsentService;
        this.piisConsentSpi = piisConsentSpi;
        this.xs2aToSpiPiisConsentMapper = xs2aToSpiPiisConsentMapper;
        this.commonDecoupledPiisService = commonDecoupledPiisService;
        this.piisScaAuthorisationService = piisScaAuthorisationService;
        this.piisScaAuthorisationServiceResolver = piisScaAuthorisationServiceResolver;
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

        ConsentAuthorizationService service = piisScaAuthorisationServiceResolver.getService();

        Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                        .psuData(psuIdData)
                                                                        .consentId(consentId)
                                                                        .authorisationId(authorisationId)
                                                                        .scaStatus(scaStatus)
                                                                        .scaApproach(scaApproach)
                                                                        .build();

        Optional<CreateConsentAuthorizationResponse> consentAuthorizationResponse = service.createConsentAuthorization(createAuthorisationRequest);

        if (consentAuthorizationResponse.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(PIIS_403)
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
                   .orElseThrow(() -> new IllegalArgumentException("PIIS authorisation service were not found for approach: " + scaApproach));
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
    void findAndTerminateOldConsents(String encryptedNewConsentId, PiisConsent piisConsent) {
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
    SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, CommonAuthorisationParameters request, PsuIdData psuData, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.verifyScaAuthorisation(spiContextData,
                                                     xs2aToSpiPiisConsentMapper.toSpiScaConfirmation(request, psuData),
                                                     xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
                                                     spiAspspConsentDataProvider);
    }

    @Override
    protected SpiResponse<SpiStartAuthorisationResponse> getSpiStartAuthorisationResponse(SpiContextData spiContextData, ScaApproach scaApproach, ScaStatus scaStatus, String authorisationId, PiisConsent consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        return piisConsentSpi.startAuthorisation(spiContextData, SpiScaApproach.valueOf(scaApproach.name()), SpiScaStatus.valueOf(scaStatus.name()), authorisationId, xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent), spiAspspConsentDataProvider);
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
