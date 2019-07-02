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
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_401;

@Slf4j
@Service("AIS_RECEIVED")
public class AisScaReceivedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final SpiContextDataProvider spiContextDataProvider;
    private final ScaApproachResolver scaApproachResolver;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final AisScaAuthorisationService aisScaAuthorisationService;
    private final RequestProviderService requestProviderService;

    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    public AisScaReceivedAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                            AisConsentDataService aisConsentDataService,
                                            AisConsentSpi aisConsentSpi,
                                            Xs2aAisConsentMapper aisConsentMapper,
                                            Xs2aToSpiPsuDataMapper psuDataMapper,
                                            SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                            SpiContextDataProvider spiContextDataProvider,
                                            SpiErrorMapper spiErrorMapper,
                                            ScaApproachResolver scaApproachResolver,
                                            CommonDecoupledAisService commonDecoupledAisService,
                                            AisScaAuthorisationService aisScaAuthorisationService,
                                            RequestProviderService requestProviderService) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.scaApproachResolver = scaApproachResolver;
        this.commonDecoupledAisService = commonDecoupledAisService;
        this.aisScaAuthorisationService = aisScaAuthorisationService;
        this.requestProviderService = requestProviderService;
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
        String consentId = request.getConsentId();

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            log.warn("X-Request-ID: [{}], Consent-ID [{}]. AIS_RECEIVED stage. Apply Authorisation when update consent PSU data has failed. Consent not found by id.",
                     requestProviderService.getRequestId(), consentId);
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList(), request);
        }

        String authorisationId = request.getAuthorizationId();
        PsuIdData psuData = extractPsuIdData(request);
        String psuId = psuData.getPsuId();

        AccountConsent accountConsent = accountConsentOptional.get();
        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextDataProvider.provideWithPsuIdData(psuData), psuDataMapper.mapToSpiPsuData(psuData), request.getPassword(), spiAccountConsent, aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.hasError()) {
            if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
                log.warn("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_RECEIVED stage. Authorise PSU when Apply AIS Authorisation has failed. PSU credentials invalid.",
                         requestProviderService.getRequestId(), consentId, authorisationId, psuId);
                MessageError messageError = new MessageError(AIS_401, of(PSU_CREDENTIALS_INVALID));
                UpdateConsentPsuDataResponse failedResponse = createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages(), request);

                aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(failedResponse, request));

                return failedResponse;
            }

            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            log.warn("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_RECEIVED stage. Authorise PSU when Apply AIS Authorisation has failed. Error msg: [{}]",
                     requestProviderService.getRequestId(), consentId, authorisationId, psuId, messageError);
            return createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages(), request);
        }

        if (aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent.isConsentForAllAvailableAccounts(), accountConsent.isOneAccessType())) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.VALID);

            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, authorisationId);
            response.setScaAuthenticationData(request.getScaAuthenticationData());
            return response;
        }

        SpiResponse<List<SpiAuthenticationObject>> spiResponse = aisConsentSpi.requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData), spiAccountConsent, aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            log.warn("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_RECEIVED stage. Request available ScaMethods when apply AIS Authorisation has failed. Error msg: [{}]",
                     requestProviderService.getRequestId(), consentId, authorisationId, psuId, messageError);
            return createFailedResponse(messageError, spiResponse.getMessages(), request);
        }

        List<SpiAuthenticationObject> availableScaMethods = spiResponse.getPayload();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            aisConsentService.saveAuthenticationMethods(authorisationId, spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));

            if (availableScaMethods.size() > 1) {
                return createResponseForMultipleAvailableMethods(availableScaMethods, authorisationId, consentId);
            } else {
                return createResponseForOneAvailableMethod(request, spiAccountConsent, availableScaMethods.get(0), psuData);
            }
        } else {
            log.info("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_RECEIVED stage. Apply AIS Authorisation has failed. Consent rejected because no available SCA methods.",
                     requestProviderService.getRequestId(), consentId, authorisationId, psuId);
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
            UpdateConsentPsuDataResponse response = createResponseForNoneAvailableScaMethod(consentId, authorisationId);
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, request));
            return response;
        }
    }

    private UpdateConsentPsuDataResponse applyIdentification(UpdateConsentPsuDataReq request) {
        if (!isPsuExist(request.getPsuData())) {
            log.warn("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}]. AIS_RECEIVED stage. Apply Identification when update consent PSU data has failed. No PSU data available in request.",
                     requestProviderService.getRequestId(), request.getConsentId(), request.getAuthorizationId());
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
            return createFailedResponse(messageError, Collections.singletonList(MESSAGE_ERROR_NO_PSU), request);
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getConsentId(), request.getAuthorizationId());
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<SpiAuthenticationObject> availableScaMethods, String authorisationId, String consentId) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED, consentId, authorisationId);
        response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));
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
        String authenticationMethodId = scaMethod.getAuthenticationMethodId();
        AspspConsentData aspspConsentData = aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId());
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationMethodId, spiAccountConsent, aspspConsentData);
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS));
            log.warn("X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], Authentication-Method-ID [{}]. AIS_RECEIVED stage. Proceed embedded approach when performs authorisation depending on selected SCA method has failed. Error msg: [{}]",
                     requestProviderService.getRequestId(), request.getConsentId(), request.getAuthorizationId(), request.getPsuData().getPsuId(), authenticationMethodId, messageError);
            return createFailedResponse(messageError, spiResponse.getMessages(), request);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, request.getConsentId(), request.getAuthorizationId());
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(scaMethod));
        response.setChallengeData(challengeData);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForNoneAvailableScaMethod(String consentId, String authorisationId) {

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FAILED, consentId, authorisationId);
        response.setMessageError(new MessageError(ErrorType.AIS_400, of(SCA_METHOD_UNKNOWN)));
        return response;
    }
}
