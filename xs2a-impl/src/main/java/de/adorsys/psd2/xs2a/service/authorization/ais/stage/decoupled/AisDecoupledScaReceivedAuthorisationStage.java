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
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
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

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;

@Slf4j
@Service("AIS_DECOUPLED_RECEIVED")
public class AisDecoupledScaReceivedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

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
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq updateConsentPsuDataReq) {
        return updateConsentPsuDataReq.isUpdatePsuIdentification()
                   ? applyIdentification(updateConsentPsuDataReq)
                   : applyAuthorisation(updateConsentPsuDataReq);
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(UpdateConsentPsuDataReq updateConsentPsuDataReq) {
        String consentId = updateConsentPsuDataReq.getConsentId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Apply authorisation when update consent PSU data has failed. Consent not found by id.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);
            MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList(), updateConsentPsuDataReq);
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);
        PsuIdData psuData = extractPsuIdData(updateConsentPsuDataReq);


        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextData,
                                                                                                             spiPsuData,
                                                                                                             updateConsentPsuDataReq.getPassword(),
                                                                                                             spiAccountConsent,
                                                                                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (authorisationStatusSpiResponse.hasError()) {

            if (authorisationStatusSpiResponse.getPayload() != null &&
                    authorisationStatusSpiResponse.getPayload().getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
                log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Authorise PSU when apply authorisation with update consent PSU data has failed. PSU credentials invalid.",
                         requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, updateConsentPsuDataReq.getAuthorizationId(), spiPsuData.getPsuId());
                MessageError messageError = new MessageError(ErrorType.AIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
                return createFailedResponse(messageError, authorisationStatusSpiResponse.getErrors(), updateConsentPsuDataReq);
            }

            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Authorise PSU when apply authorisation with update consent PSU data has failed. Error msg: [{}].",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, updateConsentPsuDataReq.getAuthorizationId(), spiPsuData.getPsuId(), messageError);
            return createFailedResponse(messageError, authorisationStatusSpiResponse.getErrors(), updateConsentPsuDataReq);
        }

        if (aisScaAuthorisationService.isOneFactorAuthorisation(accountConsent)) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.VALID);

            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, updateConsentPsuDataReq.getAuthorizationId());
            response.setScaAuthenticationData(updateConsentPsuDataReq.getScaAuthenticationData());
            return response;
        }

        return commonDecoupledAisService.proceedDecoupledApproach(updateConsentPsuDataReq, spiAccountConsent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(UpdateConsentPsuDataReq request) {
        if (!isPsuExist(request.getPsuData())) {
            log.warn("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}]. AIS_DECOUPLED_RECEIVED stage. Apply identification when update consent PSU data has failed. No PSU data available in request.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getConsentId(), request.getAuthorizationId());
            MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
            return createFailedResponse(messageError, Collections.singletonList(new TppMessage(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU)), request);
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getConsentId(), request.getAuthorizationId());
    }
}
