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
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;

@Service("AIS_STARTED")
public class AisScaStartAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaStartAuthorisationStage(AisConsentService aisConsentService,
                                         AisConsentDataService aisConsentDataService,
                                         AisConsentSpi aisConsentSpi,
                                         Xs2aAisConsentMapper aisConsentMapper,
                                         SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                         Xs2aToSpiPsuDataMapper psuDataMapper,
                                         SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper);
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
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());
        PsuIdData psuData = request.getPsuData();

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(psuDataMapper.mapToSpiPsuData(psuData), request.getPassword(), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.hasError()) {
            if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
                return createFailedResponse(MessageErrorCode.PSU_CREDENTIALS_INVALID);
            }

            return createFailedResponse(messageErrorCodeMapper.mapToMessageErrorCode(authorisationStatusSpiResponse.getResponseStatus()));
        }

        SpiResponse<List<SpiAuthenticationObject>> spiResponse = aisConsentSpi.requestAvailableScaMethods(psuDataMapper.mapToSpiPsuData(psuData), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            return createFailedResponse(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
        }

        List<SpiAuthenticationObject> availableScaMethods = spiResponse.getPayload();

        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            if (availableScaMethods.size() > 1) {
                return createResponseForMultipleAvailableMethods(psuData, availableScaMethods);
            } else {
                return createResponseForOneAvailableMethod(accountConsent, psuData, availableScaMethods.get(0), request.getConsentId());
            }
        } else {
            aisConsentService.updateConsentStatus(request.getConsentId(), SpiConsentStatus.REJECTED);
            return createResponseForNoneAvailableScaMethod(psuData);
        }
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(PsuIdData psuData, List<SpiAuthenticationObject> availableScaMethods) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setPsuId(psuData.getPsuId());
        response.setAvailableScaMethods(spiToXs2aAuthenticationObjectMapper.mapToXs2aListAuthenticationObject(availableScaMethods));
        response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(SpiAccountConsent accountConsent, PsuIdData psuData, SpiAuthenticationObject scaMethod, String consentId) {
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(psuDataMapper.mapToSpiPsuData(psuData), scaMethod.getAuthenticationMethodId(), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return response;
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setPsuId(psuData.getPsuId());
        response.setChosenScaMethod(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(scaMethod));
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForNoneAvailableScaMethod(PsuIdData psuData) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setPsuId(psuData.getPsuId());
        response.setScaStatus(ScaStatus.FAILED);
        response.setErrorCode(MessageErrorCode.SCA_METHOD_UNKNOWN);
        return response;
    }
}
