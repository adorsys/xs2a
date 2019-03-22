/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded;

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.*;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;


@Service("AIS_STARTED")
public class AisScaStartAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final SpiContextDataProvider spiContextDataProvider;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final ScaApproachResolver scaApproachResolver;
    private final CommonDecoupledAisService commonDecoupledAisService;

    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    public AisScaStartAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                         AisConsentDataService aisConsentDataService,
                                         AisConsentSpi aisConsentSpi,
                                         Xs2aAisConsentMapper aisConsentMapper,
                                         SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                         Xs2aToSpiPsuDataMapper psuDataMapper,
                                         SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                         SpiContextDataProvider spiContextDataProvider,
                                         AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                         SpiErrorMapper spiErrorMapper,
                                         ScaApproachResolver scaApproachResolver,
                                         CommonDecoupledAisService commonDecoupledAisService) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
        this.scaApproachResolver = scaApproachResolver;
        this.commonDecoupledAisService = commonDecoupledAisService;
    }

    /**
     * Start authorisation stage workflow: SPU authorising process using data from request
     * (returns response with FAILED status in case of non-successful authorising), available SCA methods getting
     * and performing the flow according to none, one or multiple available methods.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(request)
                   : applyAuthorisation(request);
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(UpdateConsentPsuDataReq request) {
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(request.getConsentId());
        if (!accountConsentOptional.isPresent()) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList());
        }
        PsuIdData psuData = extractPsuIdData(request);

        AccountConsent accountConsent = accountConsentOptional.get();
        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextDataProvider.provideWithPsuIdData(psuData), psuDataMapper.mapToSpiPsuData(psuData), request.getPassword(), spiAccountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.hasError()) {
            if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
                MessageError messageError = new MessageError(AIS_401, of(PSU_CREDENTIALS_INVALID));
                UpdateConsentPsuDataResponse failedResponse = createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages());

                aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(failedResponse, request));

                return failedResponse;
            }

            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages());
        }

        // TODO Extract common consent validation from AIS Embedded and Decoupled stages https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/677
        if (isOneFactorAuthorisation(accountConsent)) {

            aisConsentService.updateConsentStatus(request.getConsentId(), ConsentStatus.VALID);

            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
            response.setScaAuthenticationData(request.getScaAuthenticationData());
            response.setScaStatus(ScaStatus.FINALISED);
            return response;
        }

        SpiResponse<List<SpiAuthenticationObject>> spiResponse = aisConsentSpi.requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData), spiAccountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, spiResponse.getMessages());
        }

        List<SpiAuthenticationObject> availableScaMethods = spiResponse.getPayload();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            aisConsentService.saveAuthenticationMethods(request.getAuthorizationId(), spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));

            if (availableScaMethods.size() > 1) {
                return createResponseForMultipleAvailableMethods(availableScaMethods);
            } else {
                return createResponseForOneAvailableMethod(request, spiAccountConsent, availableScaMethods.get(0), psuData);
            }
        } else {
            aisConsentService.updateConsentStatus(request.getConsentId(), ConsentStatus.REJECTED);
            UpdateConsentPsuDataResponse response = createResponseForNoneAvailableScaMethod();
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, request));
            return response;
        }
    }

    private boolean isOneFactorAuthorisation(AccountConsent accountConsent) {
        return accountConsent.getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS
                   && accountConsent.isOneAccessType()
                   && !aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()
                   && !accountConsent.isMultilevelScaRequired();
    }

    private UpdateConsentPsuDataResponse applyIdentification(UpdateConsentPsuDataReq request) {
        if (!isPsuExist(request.getPsuData())) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
            return createFailedResponse(messageError, Collections.singletonList(MESSAGE_ERROR_NO_PSU));
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaStatus(ScaStatus.PSUIDENTIFIED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<SpiAuthenticationObject> availableScaMethods) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));
        response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(UpdateConsentPsuDataReq request, SpiAccountConsent spiAccountConsent, SpiAuthenticationObject scaMethod, PsuIdData psuData) {
        if (scaMethod.isDecoupled()) {
            scaApproachResolver.forceDecoupledScaApproach();
            aisConsentService.updateScaApproach(request.getAuthorizationId(), ScaApproach.DECOUPLED);
            return commonDecoupledAisService.proceedDecoupledApproach(request, spiAccountConsent, scaMethod.getAuthenticationMethodId(), psuData);
        }

        return proceedEmbeddedScaApproach(request, spiAccountConsent, scaMethod);
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedScaApproach(UpdateConsentPsuDataReq request, SpiAccountConsent spiAccountConsent, SpiAuthenticationObject scaMethod) {
        PsuIdData psuData = request.getPsuData();
        AspspConsentData aspspConsentData = aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId());
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData), scaMethod.getAuthenticationMethodId(), spiAccountConsent, aspspConsentData);
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, spiResponse.getMessages());
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(scaMethod));
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
        response.setChallengeData(challengeData);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForNoneAvailableScaMethod() {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaStatus(ScaStatus.FAILED);
        response.setMessageError(new MessageError(ErrorType.AIS_400, of(SCA_METHOD_UNKNOWN)));
        return response;
    }
}
