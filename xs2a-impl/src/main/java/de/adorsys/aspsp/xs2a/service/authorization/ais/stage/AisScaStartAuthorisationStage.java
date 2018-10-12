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
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aScaStatus;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION;

@Service("AIS_STARTED")
public class AisScaStartAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {

    public AisScaStartAuthorisationStage(AisConsentService aisConsentService, AisConsentDataService aisConsentDataService, AisConsentSpi aisConsentSpi, Xs2aAisConsentMapper aisConsentMapper, SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper);
    }

    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());

        SpiPsuData psuData = new SpiPsuData(request.getPsuId(), null, null, null);  // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(psuData, request.getPassword(), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
            response.setScaStatus(Xs2aScaStatus.FAILED);
            return response;
        }

        response.setPsuId(request.getPsuId());
        response.setPassword(request.getPassword());

        SpiResponse<List<SpiScaMethod>> spiResponse = aisConsentSpi.requestAvailableScaMethods(psuData, accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        List<SpiScaMethod> availableScaMethods = spiResponse.getPayload();

        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            if (isMultipleScaMethods(availableScaMethods)) {
                proceedResponseForMultipleAvailableMethods(response, availableScaMethods);
            } else {
                proceedResponseForOneAvailableMethod(response, accountConsent, availableScaMethods, request.getConsentId());
            }
        } else {
            proceedResponseForNoneAvailableScaMethod(response, request.getConsentId());
        }

        return response;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> availableMethods) {
        return availableMethods.size() > 1;
    }

    private void proceedResponseForMultipleAvailableMethods(UpdateConsentPsuDataResponse response, List<SpiScaMethod> availableScaMethods) {
        response.setAvailableScaMethods(aisConsentMapper.mapToCmsScaMethods(availableScaMethods));
        response.setScaStatus(Xs2aScaStatus.PSUAUTHENTICATED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    private void proceedResponseForOneAvailableMethod(UpdateConsentPsuDataResponse response, SpiAccountConsent accountConsent, List<SpiScaMethod> availableScaMethods, String consentId) {
        SpiPsuData psuData = new SpiPsuData(response.getPsuId(), null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(psuData, availableScaMethods.get(0), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return;
        }

        response.setChosenScaMethod(availableScaMethods.get(0).name());
        response.setScaStatus(Xs2aScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
    }

    private void proceedResponseForNoneAvailableScaMethod(UpdateConsentPsuDataResponse response, String consentId) {
        response.setScaStatus(Xs2aScaStatus.FAILED);
        response.setErrorCode(MessageErrorCode.SCA_METHOD_UNKNOWN);
        aisConsentService.updateConsentStatus(consentId, SpiConsentStatus.REJECTED);
    }
}
