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

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
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
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;

@Service("AIS_SCAMETHODSELECTED")
public class AisScaAuthenticatedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final SpiContextDataProvider spiContextDataProvider;

    public AisScaAuthenticatedStage(Xs2aAisConsentService aisConsentService,
                                    AisConsentDataService aisConsentDataService,
                                    AisConsentSpi aisConsentSpi,
                                    Xs2aAisConsentMapper aisConsentMapper,
                                    SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                    Xs2aToSpiPsuDataMapper psuDataMapper,
                                    SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                    SpiContextDataProvider spiContextDataProvider,
                                    SpiErrorMapper spiErrorMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
    }

    /**
     * Verifying authorisation code workflow: verifying code from the request
     * (returns response with error code in case of wrong code being provided), updates consent status
     * and returns response with FINALISED status.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        String consentId = request.getConsentId();
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, of(MessageErrorCode.CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList());
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        PsuIdData psuData = extractPsuIdData(request);

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = aisConsentSpi.verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData), aisConsentMapper.mapToSpiScaConfirmation(request, psuData), aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, spiResponse.getMessages());
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !accountConsent.isMultilevelScaRequired()) {
            aisConsentService.updateMultilevelScaRequired(consentId, true);
        }

        if (accountConsent.getConsentStatus() != responseConsentStatus) {
            aisConsentService.updateConsentStatus(consentId, responseConsentStatus);
        }
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaAuthenticationData(request.getScaAuthenticationData());
        response.setScaStatus(ScaStatus.FINALISED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        return response;
    }
}
