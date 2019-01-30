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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aFundsConfirmationMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PiisConsentValidationService;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIIS_400;

@Slf4j
@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AspspProfileServiceWrapper profileService;
    private final FundsConfirmationSpi fundsConfirmationSpi;
    private final FundsConfirmationConsentDataService fundsConfirmationConsentDataService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    private final SpiToXs2aFundsConfirmationMapper spiToXs2aFundsConfirmationMapper;
    private final PiisConsentValidationService piisConsentValidationService;
    private final PiisConsentService piisConsentService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    /**
     * Checks if the account balance is sufficient for requested operation
     *
     * @param request Contains the requested balanceAmount in order to compare with the available balanceAmount in the account
     * @return Response with the result 'true' if there are enough funds in the account, 'false' otherwise
     */
    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        xs2aEventService.recordTppRequest(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED, request);

        PiisConsent consent = null;
        if (profileService.isPiisConsentSupported()) {
            AccountReference accountReference = request.getPsuAccount();
            PiisConsentValidationResult validationResult = validateAccountReference(accountReference);

            if (validationResult.hasError()) {
                ErrorHolder errorHolder = validationResult.getErrorHolder();
                return ResponseObject.<FundsConfirmationResponse>builder()
                           .fail(new MessageError(errorHolder))
                           .build();
            }

            consent = validationResult.getConsent();
        }

        PsuIdData psuIdData = getPsuIdData(consent);
        AspspConsentData aspspConsentData = getAspspConsentData(consent);
        FundsConfirmationResponse response = executeRequest(psuIdData, consent, request, aspspConsentData);

        if (response.hasError()) {
            ErrorHolder errorHolder = response.getErrorHolder();
            return ResponseObject.<FundsConfirmationResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        return ResponseObject.<FundsConfirmationResponse>builder()
                   .body(response)
                   .build();
    }

    private PiisConsentValidationResult validateAccountReference(AccountReference accountReference) {
        AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();

        if (selector == null) {
            log.warn("No account identifier in the request {}", accountReference);
            return new PiisConsentValidationResult(ErrorHolder.builder(FORMAT_ERROR).errorType(PIIS_400).build());
        }

        List<PiisConsent> response = piisConsentService.getPiisConsentListByAccountIdentifier(accountReference.getCurrency(),
                                                                                              selector);

        return piisConsentValidationService.validatePiisConsentData(response);
    }

    private FundsConfirmationResponse executeRequest(@NotNull PsuIdData psuIdData,
                                                     @Nullable PiisConsent consent,
                                                     @NotNull FundsConfirmationRequest request,
                                                     @NotNull AspspConsentData aspspConsentData) {
        SpiFundsConfirmationRequest spiRequest = xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(request);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);

        SpiResponse<SpiFundsConfirmationResponse> fundsSufficientCheck = fundsConfirmationSpi.performFundsSufficientCheck(
            spiContextData,
            consent,
            spiRequest,
            aspspConsentData
        );

        if (consent != null) {
            AspspConsentData newAspspConsentData = fundsSufficientCheck.getAspspConsentData();
            fundsConfirmationConsentDataService.updateAspspConsentData(newAspspConsentData);
        }

        if (fundsSufficientCheck.hasError()) {
                return new FundsConfirmationResponse(spiErrorMapper.mapToErrorHolder(fundsSufficientCheck, ServiceType.PIIS));
        }

        return spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(fundsSufficientCheck.getPayload());
    }

    private @NotNull PsuIdData getPsuIdData(@Nullable PiisConsent consent) {
        // TODO Extract PSU Data from request if it's possible https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/525
        PsuIdData emptyPsuIdData = new PsuIdData(null, null, null, null);
        if (consent == null) {
            return emptyPsuIdData;
        }

        return Optional.ofNullable(consent.getPsuData())
                   .orElse(emptyPsuIdData);
    }

    private @NotNull AspspConsentData getAspspConsentData(@Nullable PiisConsent consent) {
        if (consent == null) {
            // TODO Do not pass AspspConsentData at all if there is no PIIS consent https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/526
            return AspspConsentData.emptyConsentData();
        }

        return fundsConfirmationConsentDataService.getAspspConsentData(consent.getId());
    }
}
