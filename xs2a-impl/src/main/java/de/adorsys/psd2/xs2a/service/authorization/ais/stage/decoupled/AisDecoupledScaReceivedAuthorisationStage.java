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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage.decoupled;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.CommonDecoupledAisService;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("AIS_DECOUPLED_RECEIVED")
public class AisDecoupledScaReceivedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, AccountConsentAuthorization, UpdateConsentPsuDataResponse> {

    private final RequestProviderService requestProviderService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final CommonDecoupledAisService commonDecoupledAisService;
    private final AisScaAuthorisationService aisScaAuthorisationService;

    public AisDecoupledScaReceivedAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                                     SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                     RequestProviderService requestProviderService,
                                                     AisConsentSpi aisConsentSpi,
                                                     Xs2aAisConsentMapper aisConsentMapper,
                                                     Xs2aToSpiPsuDataMapper psuDataMapper,
                                                     SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                     SpiErrorMapper spiErrorMapper,
                                                     SpiContextDataProvider spiContextDataProvider,
                                                     CommonDecoupledAisService commonDecoupledAisService,
                                                     AisScaAuthorisationService aisScaAuthorisationService) {
        super(aisConsentService, aspspConsentDataProviderFactory, aisConsentSpi, aisConsentMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.commonDecoupledAisService = commonDecoupledAisService;
        this.aisScaAuthorisationService = aisScaAuthorisationService;
        this.requestProviderService = requestProviderService;
    }

    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq updateConsentPsuDataReq, AccountConsentAuthorization authorisationResponse) {
        return updateConsentPsuDataReq.isUpdatePsuIdentification()
                   ? applyIdentification(updateConsentPsuDataReq)
                   : applyAuthorisation(updateConsentPsuDataReq, authorisationResponse);
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(UpdateConsentPsuDataReq request, AccountConsentAuthorization authorisationResponse) {
        String consentId = request.getConsentId();
        String authorisationId = request.getAuthorisationId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Apply authorisation when update consent PSU data has failed. Consent not found by id. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, authorisationResponse);

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextData,
                                                                                                             spiPsuData,
                                                                                                             request.getPassword(),
                                                                                                             spiAccountConsent,
                                                                                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (authorisationStatusSpiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS);
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Authorise PSU when apply authorisation with update consent PSU data has failed. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, request.getAuthorizationId(), spiPsuData.getPsuId(), errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        if (authorisationStatusSpiResponse.getPayload().getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_401)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Authorise PSU when apply authorisation with update consent PSU data has failed. PSU credentials invalid. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, request.getAuthorizationId(), spiPsuData.getPsuId(), errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        if (aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.VALID);

            return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorizationId());
        }

        return commonDecoupledAisService.proceedDecoupledApproach(request.getConsentId(), request.getAuthorisationId(), spiAccountConsent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(UpdateConsentPsuDataReq request) {
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Apply identification when update consent PSU data has failed. No PSU data available in request. Error msg: [{}]",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getConsentId(), request.getAuthorizationId(), errorHolder);
            return new UpdateConsentPsuDataResponse(errorHolder, request.getConsentId(), request.getAuthorisationId());
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getConsentId(), request.getAuthorizationId());
    }
}
