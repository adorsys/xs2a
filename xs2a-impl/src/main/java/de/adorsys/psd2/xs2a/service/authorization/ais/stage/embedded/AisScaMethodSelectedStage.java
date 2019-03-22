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

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
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
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;

@Service("AIS_PSUAUTHENTICATED")
public class AisScaMethodSelectedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final SpiContextDataProvider spiContextDataProvider;
    private final ScaApproachResolver scaApproachResolver;
    private final CommonDecoupledAisService commonDecoupledAisService;


    public AisScaMethodSelectedStage(Xs2aAisConsentService aisConsentService,
                                     AisConsentDataService aisConsentDataService,
                                     AisConsentSpi aisConsentSpi,
                                     Xs2aAisConsentMapper aisConsentMapper,
                                     SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                     Xs2aToSpiPsuDataMapper psuDataMapper,
                                     SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                     SpiContextDataProvider spiContextDataProvider,
                                     SpiErrorMapper spiErrorMapper,
                                     ScaApproachResolver scaApproachResolver,
                                     CommonDecoupledAisService commonDecoupledAisService) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.scaApproachResolver = scaApproachResolver;
        this.commonDecoupledAisService = commonDecoupledAisService;
    }

    /**
     * Stage for multiple available SCA methods only: request should contain chosen sca method,
     * that is used in a process of requesting authorisation code (returns response with error code in case of wrong code request)
     * and returns response with SCAMETHODSELECTED status.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(request.getConsentId());
        if (!accountConsentOptional.isPresent()) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(MessageErrorCode.CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList());
        }

        PsuIdData psuData = extractPsuIdData(request);
        AccountConsent accountConsent = accountConsentOptional.get();

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);

        String authenticationMethodId = request.getAuthenticationMethodId();
        if (isDecoupledApproach(request.getAuthorizationId(), authenticationMethodId)) {
            scaApproachResolver.forceDecoupledScaApproach();
            aisConsentService.updateScaApproach(request.getAuthorizationId(), ScaApproach.DECOUPLED);
            return commonDecoupledAisService.proceedDecoupledApproach(request, spiAccountConsent, authenticationMethodId, psuData);
        }

        return proceedEmbeddedApproach(request, spiAccountConsent, psuData);
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return aisConsentService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedApproach(UpdateConsentPsuDataReq request, SpiAccountConsent spiAccountConsent, PsuIdData psuData) {
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData), request.getAuthenticationMethodId(), spiAccountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, spiResponse.getMessages());
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        SpiAuthenticationObject chosenScaMethod = authorizationCodeResult.getSelectedScaMethod();
        ChallengeData challengeData = authorizationCodeResult.getChallengeData();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenScaMethod));
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
        response.setChallengeData(challengeData);
        return response;
    }
}
