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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.Xs2aCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aCurrencyConversionInfoMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCurrencyConversionInfo;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CurrencyConversionInfoSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.PaymentCancellationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType.PIS_CREATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class PisCommonDecoupledService {
    private final PaymentAuthorisationSpi paymentAuthorisationSpi;
    private final PaymentCancellationSpi paymentCancellationSpi;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final CurrencyConversionInfoSpi currencyConversionInfoSpi;
    private final SpiToXs2aCurrencyConversionInfoMapper spiToXs2aCurrencyConversionInfoMapper;

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledInitiation(PaymentAuthorisationParameters request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, PIS_CREATION);
    }

    public Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupledCancellation(PaymentAuthorisationParameters request, SpiPayment payment, String authenticationMethodId) {
        return proceedDecoupled(request, payment, authenticationMethodId, AuthorisationType.PIS_CANCELLATION);
    }

    private Xs2aUpdatePisCommonPaymentPsuDataResponse proceedDecoupled(PaymentAuthorisationParameters request, SpiPayment payment,
                                                                       String authenticationMethodId, AuthorisationType authorisationType) {
        String authenticationId = request.getAuthorisationId();
        String paymentId = request.getPaymentId();
        PsuIdData psuData = request.getPsuData();
        SpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getPaymentId());
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse;
        SpiResponse<SpiCurrencyConversionInfo> conversionSpiResponse = null;

        switch (authorisationType) {
            case PIS_CREATION:
                spiResponse = paymentAuthorisationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationId, authenticationMethodId, payment, aspspConsentDataProvider);
                conversionSpiResponse = currencyConversionInfoSpi.getCurrencyConversionInfo(spiContextDataProvider.provideWithPsuIdData(psuData), payment, authenticationId, aspspConsentDataProvider);
                break;
            case PIS_CANCELLATION:
                spiResponse = paymentCancellationSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authenticationId, authenticationMethodId, payment, aspspConsentDataProvider);
                break;
            default:
                log.info("Payment-ID [{}]. Payment Authorisation Type {} is not supported.", paymentId, authorisationType);
                throw new IllegalArgumentException("This SCA type is not supported: " + authorisationType);
        }

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS);
            log.warn("Payment-ID [{}], Authorisation-ID [{}], PSU-ID [{}]. Start SPI Authorisation Decoupled SCA has failed. Error msg: {}.",
                     paymentId, authenticationId, psuData.getPsuId(), errorHolder);

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                xs2aAuthorisationService.updateAuthorisationStatus(authenticationId, ScaStatus.FAILED);
            }
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, paymentId, authenticationId, psuData);
        }

        Xs2aCurrencyConversionInfo xs2aCurrencyConversionInfo = Optional.ofNullable(conversionSpiResponse)
                                                                    .map(SpiResponse::getPayload)
                                                                    .map(spiToXs2aCurrencyConversionInfoMapper::toXs2aCurrencyConversionInfo)
                                                                    .orElse(null);
        SpiAuthorisationDecoupledScaResponse spiAuthorisationDecoupledScaResponse = spiResponse.getPayload();

        Xs2aUpdatePisCommonPaymentPsuDataResponse response = new Xs2aUpdatePisCommonPaymentPsuDataResponse(
            spiAuthorisationDecoupledScaResponse.getScaStatus(),
            request.getPaymentId(), request.getAuthorisationId(), psuData, xs2aCurrencyConversionInfo);
        response.setPsuMessage(spiAuthorisationDecoupledScaResponse.getPsuMessage());
        return response;
    }
}

