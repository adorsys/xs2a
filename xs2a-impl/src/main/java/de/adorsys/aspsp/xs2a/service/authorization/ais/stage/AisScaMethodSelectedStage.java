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

package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;

@Service("AIS_PSUAUTHENTICATED")
public class AisScaMethodSelectedStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaMethodSelectedStage(AisConsentService aisConsentService,
                                     AisConsentDataService aisConsentDataService,
                                     AisConsentSpi aisConsentSpi,
                                     Xs2aAisConsentMapper aisConsentMapper,
                                     SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                     Xs2aToSpiPsuDataMapper psuDataMapper,
                                     SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper);
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
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());
        String authenticationMethodId = request.getAuthenticationMethodId();

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(psuDataMapper.mapToSpiPsuData(request.getPsuData()), authenticationMethodId, accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return createFailedResponse(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
        }

        SpiResponse<List<SpiAuthenticationObject>> spiScaMethodsResponse = aisConsentSpi.requestAvailableScaMethods(psuDataMapper.mapToSpiPsuData(request.getPsuData()), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiScaMethodsResponse.hasError()) {
            return createFailedResponse(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
        }

        List<SpiAuthenticationObject> availableScaMethods = spiScaMethodsResponse.getPayload();
        SpiAuthenticationObject chosenScaMethod = availableScaMethods.stream()
                                                      .filter(a -> authenticationMethodId.equals(a.getAuthenticationMethodId()))
                                                      .findFirst()
                                                      .orElse(null);

        if (chosenScaMethod == null) {
            return new UpdateConsentPsuDataResponse(MessageErrorCode.SCA_METHOD_UNKNOWN);
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(chosenScaMethod));
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
        return response;
    }
}
