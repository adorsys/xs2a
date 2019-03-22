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

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageError;
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
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;


@Service("AIS_DECOUPLED_STARTED")
public class AisDecoupledScaStartAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private final SpiContextDataProvider spiContextDataProvider;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final CommonDecoupledAisService commonDecoupledAisService;

    public AisDecoupledScaStartAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                                  AisConsentDataService aisConsentDataService,
                                                  AisConsentSpi aisConsentSpi,
                                                  Xs2aAisConsentMapper aisConsentMapper,
                                                  SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                                  Xs2aToSpiPsuDataMapper psuDataMapper,
                                                  SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                  SpiErrorMapper spiErrorMapper,
                                                  SpiContextDataProvider spiContextDataProvider,
                                                  AspspProfileServiceWrapper aspspProfileServiceWrapper,
                                                  CommonDecoupledAisService commonDecoupledAisService) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
        this.commonDecoupledAisService = commonDecoupledAisService;
    }

    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq updateConsentPsuDataReq) {
        return updateConsentPsuDataReq.isUpdatePsuIdentification()
                   ? applyIdentification(updateConsentPsuDataReq)
                   : applyAuthorisation(updateConsentPsuDataReq);
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(UpdateConsentPsuDataReq updateConsentPsuDataReq) {
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(updateConsentPsuDataReq.getConsentId());

        if (!accountConsentOptional.isPresent()) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400));
            return createFailedResponse(messageError, Collections.emptyList());
        }
        AccountConsent accountConsent = accountConsentOptional.get();

        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsent);
        PsuIdData psuData = extractPsuIdData(updateConsentPsuDataReq);


        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuData);
        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(spiContextData,
                                                                                                        spiPsuData,
                                                                                                        updateConsentPsuDataReq.getPassword(),
                                                                                                        spiAccountConsent,
                                                                                                        aisConsentDataService.getAspspConsentDataByConsentId(updateConsentPsuDataReq.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.hasError()) {
            if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
                MessageError messageError = new MessageError(ErrorType.AIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
                return createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages());
            }

            MessageError messageError = new MessageError(spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, ServiceType.AIS));
            return createFailedResponse(messageError, authorisationStatusSpiResponse.getMessages());
        }

        // TODO Extract common consent validation from AIS Embedded and Decoupled stages https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/677
        if (accountConsent.getAisConsentRequestType() == AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS
                && accountConsent.isOneAccessType()
                && !aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()) {

            aisConsentService.updateConsentStatus(updateConsentPsuDataReq.getConsentId(), ConsentStatus.VALID);

            UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
            response.setScaAuthenticationData(updateConsentPsuDataReq.getScaAuthenticationData());
            response.setScaStatus(ScaStatus.FINALISED);
            return response;
        }

        return commonDecoupledAisService.proceedDecoupledApproach(updateConsentPsuDataReq, spiAccountConsent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(UpdateConsentPsuDataReq request) {
        if (!isPsuExist(request.getPsuData())) {
            MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
            return createFailedResponse(messageError, Collections.singletonList(MESSAGE_ERROR_NO_PSU));
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        response.setScaStatus(ScaStatus.PSUIDENTIFIED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

        return response;
    }
}
