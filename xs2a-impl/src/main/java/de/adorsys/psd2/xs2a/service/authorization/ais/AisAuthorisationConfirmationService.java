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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_403;

@Slf4j
@Component
@RequiredArgsConstructor
public class AisAuthorisationConfirmationService {

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final RequestProviderService requestProviderService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentSpi aisConsentSpi;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;

    /**
     * Checks authorisation confirmation data. Has two possible flows:
     * - data is checked at XS2A side, we compare the data from DB with the incoming data;
     * - data is transferred to SPI level and checking should be implemented at ASPSP side.
     *
     * @param request {@link UpdateConsentPsuDataReq} with all consent information.
     * @return {@link Xs2aUpdatePisCommonPaymentPsuDataResponse} with new authorisation status.
     */
    public ResponseObject<UpdateConsentPsuDataResponse> processAuthorisationConfirmation(UpdateConsentPsuDataReq request) {
        String consentId = request.getConsentId();
        String authorisationId = request.getAuthorisationId();

        CmsResponse<AisConsentAuthorizationResponse> authorisation = aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(authorisationId, consentId);

        if (authorisation.hasError()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update consent PSU data failed: authorisation not found by ID",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), request.getAuthorizationId());
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_403, of(CONSENT_UNKNOWN_403)).build();
        }

        AisConsentAuthorizationResponse consentAuthorisation = authorisation.getPayload();

        ScaStatus currentStatus = consentAuthorisation.getScaStatus();

        boolean processIsAllowed = currentStatus == ScaStatus.UNCONFIRMED;

        UpdateConsentPsuDataResponse response = processIsAllowed
                                                    ? processAuthorisationConfirmationInternal(request, consentAuthorisation.getScaAuthenticationData())
                                                    : buildFormatErrorResponse(consentId, authorisationId, currentStatus);

        return Optional.ofNullable(response.getErrorHolder())
                   .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                 .fail(e)
                                 .build())
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build);
    }

    private UpdateConsentPsuDataResponse processAuthorisationConfirmationInternal(UpdateConsentPsuDataReq request, String confirmationCodeFromDb) {
        return aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()
                   ? checkAuthorisationConfirmationXs2a(request, confirmationCodeFromDb)
                   : checkAuthorisationConfirmationOnSpi(request);
    }

    private UpdateConsentPsuDataResponse checkAuthorisationConfirmationXs2a(UpdateConsentPsuDataReq request, String confirmationCodeFromDb) {
        String confirmationCodeReceived = request.getConfirmationCode();

        boolean codeCorrect = StringUtils.equals(confirmationCodeReceived, confirmationCodeFromDb);

        UpdateConsentPsuDataResponse response = codeCorrect
                                                    ? new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, request.getConsentId(), request.getAuthorisationId())
                                                    : buildScaConfirmationCodeErrorResponse(request.getConsentId(), request.getAuthorisationId());

        aisConsentService.updateConsentAuthorisationStatus(request.getAuthorisationId(), response.getScaStatus());

        return response;
    }

    private UpdateConsentPsuDataResponse checkAuthorisationConfirmationOnSpi(UpdateConsentPsuDataReq request) {
        String consentId = request.getConsentId();
        String authorisationId = request.getAuthorisationId();

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (!accountConsentOptional.isPresent()) {
            return buildConsentNotFoundErrorResponse(consentId, authorisationId);
        }

        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());
        SpiAccountConsent spiAccountConsent = aisConsentMapper.mapToSpiAccountConsent(accountConsentOptional.get());
        SpiAspspConsentDataProvider aspspDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId);

        SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse = aisConsentSpi.checkConfirmationCode(contextData, spiConfirmationCode, spiAccountConsent, aspspDataProvider);

        UpdateConsentPsuDataResponse response = spiResponse.hasError()
                                                    ? buildConfirmationCodeSpiErrorResponse(spiResponse, consentId, authorisationId)
                                                    : new UpdateConsentPsuDataResponse(spiResponse.getPayload().getScaStatus(), consentId, authorisationId);

        aisConsentService.updateConsentAuthorisationStatus(authorisationId, response.getScaStatus());

        return response;
    }

    private UpdateConsentPsuDataResponse buildFormatErrorResponse(String consentId, String authorisationId, ScaStatus currentStatus) {

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(FORMAT_ERROR_SCA_STATUS, ScaStatus.FINALISED.name(), ScaStatus.UNCONFIRMED.name(), currentStatus))
                                      .build();

        log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update consent PSU data failed: SCA status is invalid.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);


        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
    }

    private UpdateConsentPsuDataResponse buildScaConfirmationCodeErrorResponse(String consentId, String authorisationId) {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.AIS_400)
                                      .tppMessages(of(ERROR_SCA_CONFIRMATION_CODE))
                                      .build();

        log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update consent PSU data failed: confirmation code is wrong.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);

        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
    }

    private UpdateConsentPsuDataResponse buildConfirmationCodeSpiErrorResponse(SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse, String consentId, String authorisationId) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);

        log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update consent PSU data failed: error occurred at SPI.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);

        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
    }

    private UpdateConsentPsuDataResponse buildConsentNotFoundErrorResponse(String consentId, String authorisationId) {
        ErrorHolder errorHolder = ErrorHolder.builder(AIS_403)
                                      .tppMessages(of(CONSENT_UNKNOWN_403))
                                      .build();

        log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Update consent PSU data failed: consent not found by id",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId);

        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
    }

}
