/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aFundsConfirmationMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiFundsConfirmationRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.piis.PiisConsentValidation;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.FundsConfirmationSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

@Slf4j
@Service
@AllArgsConstructor
public class FundsConfirmationService {
    private final AspspProfileServiceWrapper profileService;
    private final FundsConfirmationSpi fundsConfirmationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aToSpiFundsConfirmationRequestMapper xs2aToSpiFundsConfirmationRequestMapper;
    private final SpiToXs2aFundsConfirmationMapper spiToXs2aFundsConfirmationMapper;
    private final PiisConsentValidation piisConsentValidation;
    private final PiisConsentService piisConsentService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final Xs2aPiisConsentService xs2aPiisConsentService;

    /**
     * Checks if the account balance is sufficient for requested operation
     *
     * @param request Contains the requested balanceAmount in order to compare with the available balanceAmount in the account
     * @return Response with the result 'true' if there are enough funds in the account, 'false' otherwise
     */
    public ResponseObject<FundsConfirmationResponse> fundsConfirmation(FundsConfirmationRequest request) {
        xs2aEventService.recordTppRequest(EventType.FUNDS_CONFIRMATION_REQUEST_RECEIVED, request);

        PiisConsent consent = null;
        PiisConsentSupported piisConsentSupported = profileService.getPiisConsentSupported();
        if (piisConsentSupported == PiisConsentSupported.ASPSP_CONSENT_SUPPORTED) {
            AccountReference accountReference = request.getPsuAccount();
            PiisConsentValidationResult validationResult = validateAccountReference(accountReference);

            if (validationResult.hasError()) {
                ErrorHolder errorHolder = validationResult.getErrorHolder();
                log.info("Check availability of funds validation failed: {}", errorHolder);
                return ResponseObject.<FundsConfirmationResponse>builder()
                           .fail(new MessageError(errorHolder))
                           .build();
            }

            consent = validationResult.getConsent();
        } else if (piisConsentSupported == PiisConsentSupported.TPP_CONSENT_SUPPORTED) {
            String consentId = request.getConsentId();
            if (StringUtils.isNotEmpty(consentId)) {
                Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);

                if (piisConsentOptional.isEmpty()) {
                    log.info("Consent-ID: [{}]. Get PIIS consent failed: initial consent not found by id", consentId);
                    return ResponseObject.<FundsConfirmationResponse>builder()
                               .fail(ErrorType.PIIS_403, of(MessageErrorCode.CONSENT_UNKNOWN_403))
                               .build();
                }

                consent = piisConsentOptional.get();
            }
        }

        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);

        // We don't transfer provider to the SPI level if there is no PIIS consent. Both PIIS consent and the provider
        // parameters are marked as @Nullable in SPI.
        SpiAspspConsentDataProvider aspspConsentDataProvider =
            consent != null ? aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consent.getId()) : null;

        FundsConfirmationResponse response = executeRequest(psuIdData, consent, request, aspspConsentDataProvider);

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
            log.info("Check availability of funds failed, because while validate account reference no account identifier found in the request [{}].",
                     accountReference);
            return new PiisConsentValidationResult(ErrorHolder.builder(PIIS_400)
                                                       .tppMessages(TppMessageInformation.of(FORMAT_ERROR))
                                                       .build());
        }

        CmsResponse<List<CmsConsent>> cmsResponse = piisConsentService.getPiisConsentListByAccountIdentifier(accountReference.getCurrency(),
                                                                                                             selector);
        List<CmsConsent> response = cmsResponse.isSuccessful()
                                        ? cmsResponse.getPayload()
                                        : Collections.emptyList();

        List<PiisConsent> piisConsents = response.stream()
                                             .map(xs2aPiisConsentMapper::mapToPiisConsent)
                                             .collect(Collectors.toList());

        return piisConsentValidation.validatePiisConsentData(piisConsents);
    }

    private FundsConfirmationResponse executeRequest(@NotNull PsuIdData psuIdData,
                                                     @Nullable PiisConsent consent,
                                                     @NotNull FundsConfirmationRequest request,
                                                     @Nullable SpiAspspConsentDataProvider aspspConsentDataProvider) {
        SpiFundsConfirmationRequest spiRequest = xs2aToSpiFundsConfirmationRequestMapper.mapToSpiFundsConfirmationRequest(request);
        SpiContextData spiContextData = spiContextDataProvider.provideWithPsuIdData(psuIdData);

        SpiResponse<SpiFundsConfirmationResponse> fundsSufficientCheck = fundsConfirmationSpi.performFundsSufficientCheck(
            spiContextData,
            xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(consent),
            spiRequest,
            aspspConsentDataProvider
        );

        if (fundsSufficientCheck.hasError()) {
            ErrorHolder error = spiErrorMapper.mapToErrorHolder(fundsSufficientCheck, ServiceType.PIIS);
            log.info("Check availability of funds failed, because perform funds sufficient check failed. Msg error: {}",
                     error);
            return new FundsConfirmationResponse(error);
        }

        return spiToXs2aFundsConfirmationMapper.mapToFundsConfirmationResponse(fundsSufficientCheck.getPayload());
    }
}
