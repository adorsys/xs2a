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

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aScaStatusResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public abstract class PaymentServiceForAuthorisation {
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final RequestProviderService requestProviderService;
    private final Xs2aAuthorisationService xs2aAuthorisationService;
    private final Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    private final SpiToXs2aLinksMapper spiToXs2aLinksMapper;

    /**
     * Gets SCA status response of payment authorisation
     *
     * @param paymentId       String representation of payment identifier
     * @param authorisationId String representation of authorisation identifier
     * @return Response containing SCA status of the authorisation and optionally trusted beneficiaries flag or corresponding error
     */
    public ResponseObject<Xs2aScaStatusResponse> getAuthorisationScaStatus(String paymentId, String authorisationId, PaymentType paymentType, String paymentProduct) {
        ResponseObject<PaymentScaStatus> paymentScaStatusResponse = getCMSScaStatus(paymentId, authorisationId,
                                                                                    paymentType, paymentProduct);
        if (paymentScaStatusResponse.hasError()) {
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(paymentScaStatusResponse.getError())
                       .build();
        }

        SpiContextData contextData = getSpiContextData();
        SpiAspspConsentDataProvider spiAspspConsentDataProvider = aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(paymentId);
        ScaStatus scaStatus = paymentScaStatusResponse.getBody().getScaStatus();
        SpiPayment spiPayment = xs2aToSpiPaymentMapper.mapToSpiPayment(paymentScaStatusResponse.getBody().getPisCommonPaymentResponse());

        SpiResponse<SpiScaStatusResponse> spiScaStatusResponse = getScaStatus(scaStatus, contextData, authorisationId, spiPayment, spiAspspConsentDataProvider);

        if (spiScaStatusResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiScaStatusResponse, ServiceType.PIS);
            log.info("Authorisation-ID [{}], Payment-ID [{}]. Get SCA status failed.", authorisationId, paymentId);
            return ResponseObject.<Xs2aScaStatusResponse>builder()
                       .fail(errorHolder)
                       .build();
        }

        SpiScaStatusResponse spiScaInformationPayload = spiScaStatusResponse.getPayload();

        if (scaStatus.isNotFinalisedStatus() && scaStatus != spiScaInformationPayload.getScaStatus()) {
            scaStatus = spiScaInformationPayload.getScaStatus();
            xs2aAuthorisationService.updateAuthorisationStatus(authorisationId, scaStatus);
            log.info("Authorisation-ID [{}], Payment-ID [{}]. SCA status was changed to [{}] from SPI.", authorisationId, paymentId, scaStatus);
        }

        Boolean beneficiaryFlag = scaStatus.isFinalisedStatus() ? spiScaInformationPayload.getTrustedBeneficiaryFlag() : null;
        Xs2aScaStatusResponse response = new Xs2aScaStatusResponse(scaStatus,
                                                                   beneficiaryFlag,
                                                                   spiScaInformationPayload.getPsuMessage(),
                                                                   spiToXs2aLinksMapper.toXs2aLinks(spiScaInformationPayload.getLinks()),
                                                                   spiScaInformationPayload.getTppMessageInformation()
        );

        return ResponseObject.<Xs2aScaStatusResponse>builder()
                   .body(response)
                   .build();
    }

    abstract ResponseObject<PaymentScaStatus> getCMSScaStatus(String paymentId, String authorisationId,
                                                                  PaymentType paymentType, String paymentProduct);

    abstract SpiResponse<SpiScaStatusResponse> getScaStatus(@NotNull ScaStatus scaStatus,
                                                            @NotNull SpiContextData contextData,
                                                            @NotNull String authorisationId,
                                                            @NotNull SpiPayment businessObject,
                                                            @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider);

    private SpiContextData getSpiContextData() {
        PsuIdData psuIdData = requestProviderService.getPsuIdData();
        log.info("Corresponding PSU-ID {} was provided from request.", psuIdData);
        return spiContextDataProvider.provideWithPsuIdData(psuIdData);
    }
}
