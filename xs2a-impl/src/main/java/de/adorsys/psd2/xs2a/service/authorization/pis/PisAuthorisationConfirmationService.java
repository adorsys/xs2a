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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.FINALISED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PisAuthorisationConfirmationService {

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final RequestProviderService requestProviderService;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final PisCheckAuthorisationConfirmationService pisCheckAuthorisationConfirmationService;

    /**
     * Checks authorisation confirmation data. Has two possible flows:
     * - data is checked at XS2A side, we compare the data from DB with the incoming data;
     * - data is transferred to SPI level and checking should be implemented at ASPSP side.
     *
     * @param request        {@link Xs2aUpdatePisCommonPaymentPsuDataRequest} with all payment information.
     * @param isCancellation boolean flag: true in case of cancellation flow, false in case of initiation.
     * @return {@link Xs2aUpdatePisCommonPaymentPsuDataResponse} with new authorisation status.
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse processAuthorisationConfirmation(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                      boolean isCancellation) {
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();

        CmsResponse<GetPisAuthorisationResponse> pisAuthorisationResponse = isCancellation
                                                                                ? pisAuthorisationServiceEncrypted.getPisCancellationAuthorisationById(authorisationId)
                                                                                : pisAuthorisationServiceEncrypted.getPisAuthorisationById(authorisationId);
        if (pisAuthorisationResponse.hasError()) {
            ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                          .build();
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: authorisation is not found by id.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId);
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, request.getPsuData());

        }

        GetPisAuthorisationResponse authorisationResponse = pisAuthorisationResponse.getPayload();

        ScaStatus status = authorisationResponse.getScaStatus();

        boolean processIsAllowed = status == ScaStatus.UNCONFIRMED;

        return processIsAllowed
                   ? processAuthorisationConfirmationInternal(request, pisAuthorisationResponse.getPayload(), isCancellation)
                   : buildFormatErrorResponse(paymentId, authorisationId, status, request.getPsuData());
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse processAuthorisationConfirmationInternal(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                               GetPisAuthorisationResponse getPisAuthorisationResponse,
                                                                                               boolean isCancellation) {
        return aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()
                   ? checkAuthorisationConfirmationXs2a(request, getPisAuthorisationResponse, isCancellation)
                   : checkAuthorisationConfirmationOnSpi(request, getPisAuthorisationResponse, isCancellation);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationXs2a(Xs2aUpdatePisCommonPaymentPsuDataRequest request, GetPisAuthorisationResponse pisAuthorisationResponse, boolean isCancellation) {
        boolean codeCorrect = StringUtils.equals(request.getConfirmationCode(), pisAuthorisationResponse.getScaAuthenticationData());

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = codeCorrect
                                                                 ? new Xs2aUpdatePisCommonPaymentPsuDataResponse(FINALISED, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData())
                                                                 : buildScaConfirmationCodeErrorResponse(request);

        UpdatePisCommonPaymentPsuDataRequest updatePaymentRequest = pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(response);

        updateAuthorisationInDb(isCancellation, updatePaymentRequest);

        return response;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse checkAuthorisationConfirmationOnSpi(Xs2aUpdatePisCommonPaymentPsuDataRequest request,
                                                                                          GetPisAuthorisationResponse pisAuthorisationResponse,
                                                                                          boolean isCancellation) {
        SpiContextData contextData = spiContextDataProvider.provideWithPsuIdData(request.getPsuData());
        SpiPayment payment = xs2aToSpiPaymentMapper.mapToSpiPayment(pisAuthorisationResponse, request.getPaymentService(), request.getPaymentProduct());
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());

        SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse = pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiConfirmationCode, payment, aspspConsentDataProvider);

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = spiResponse.hasError()
                                                                 ? buildConfirmationCodeSpiErrorResponse(spiResponse, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData())
                                                                 : new Xs2aUpdatePisCommonPaymentPsuDataResponse(spiResponse.getPayload().getScaStatus(), request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        UpdatePisCommonPaymentPsuDataRequest updatePaymentRequest = pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(response);

        updateAuthorisationInDb(isCancellation, updatePaymentRequest);

        return response;
    }

    private void updateAuthorisationInDb(boolean isCancellation, UpdatePisCommonPaymentPsuDataRequest updatePaymentRequest) {
        if (isCancellation) {
            pisAuthorisationServiceEncrypted.updatePisCancellationAuthorisation(updatePaymentRequest.getAuthorizationId(), updatePaymentRequest);
        } else {
            pisAuthorisationServiceEncrypted.updatePisAuthorisation(updatePaymentRequest.getAuthorizationId(), updatePaymentRequest);
        }
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildConfirmationCodeSpiErrorResponse(SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse, String paymentId, String authorisationId, PsuIdData psuData) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);

        log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update payment PSU data failed: error occurred at SPI.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildScaConfirmationCodeErrorResponse(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        String paymentId = request.getPaymentId();
        String authorisationId = request.getAuthorisationId();
        PsuIdData psuData = request.getPsuData();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        log.info("InR-ID: [{}], X-Request-ID: [{}], Payment-ID [{}], Authorisation-ID [{}]. Updating PIS authorisation PSU Data has failed: confirmation code is wrong.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), paymentId, authorisationId);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse buildFormatErrorResponse(String paymentId, String authorisationId, ScaStatus currentStatus, PsuIdData psuData) {
        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_SCA_STATUS, ScaStatus.FINALISED.name(), ScaStatus.UNCONFIRMED.name(), currentStatus))
                                      .build();

        log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation-ID: [{}]. Update payment PSU data failed: SCA status is invalid.",
                 requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);

        return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authorisationId, psuData);
    }

}
