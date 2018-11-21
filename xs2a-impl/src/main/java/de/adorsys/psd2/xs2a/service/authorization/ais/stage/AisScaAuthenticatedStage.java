/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.ais.stage;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;

@Service("AIS_SCAMETHODSELECTED")
public class AisScaAuthenticatedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaAuthenticatedStage(Xs2aAisConsentService aisConsentService,
                                    AisConsentDataService aisConsentDataService,
                                    AisConsentSpi aisConsentSpi,
                                    Xs2aAisConsentMapper aisConsentMapper,
                                    SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                    Xs2aToSpiPsuDataMapper psuDataMapper,
                                    SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper);
    }

    /**
     * Verifying authorisation code workflow: verifying code from the request
     * (returns response with error code in case of wrong code being provided), updates consent status
     * and returns response with FINALISED status.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    // TODO refactoring!!!
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        AccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());
        PsuIdData psuData = request.getPsuData();

        SpiResponse<SpiResponse.VoidResponse> spiResponse = aisConsentSpi.verifyScaAuthorisation(psuDataMapper.mapToSpiPsuData(psuData), aisConsentMapper.mapToSpiScaConfirmation(request), aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return createFailedResponse(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()), spiResponse.getMessages());
        }

        aisConsentService.updateConsentStatus(request.getConsentId(), ConsentStatus.VALID);

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaAuthenticationData(request.getScaAuthenticationData());
        response.setScaStatus(ScaStatus.FINALISED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        return response;
    }
}
