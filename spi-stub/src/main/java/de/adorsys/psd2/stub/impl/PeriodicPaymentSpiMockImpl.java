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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.stub.impl.service.PaymentServiceMock;
import de.adorsys.psd2.stub.impl.service.SpiMockData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PeriodicPaymentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class PeriodicPaymentSpiMockImpl implements PeriodicPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String PSU_MESSAGE = "Message from ASPSP to PSU";
    private final PaymentServiceMock paymentService;

    @Override
    @NotNull
    public SpiResponse<SpiPeriodicPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#initiatePayment: contextData {}, spiPeriodicPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());
        SpiPeriodicPaymentInitiationResponse response = new SpiPeriodicPaymentInitiationResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setPaymentId(UUID.randomUUID().toString());
        response.setAspspAccountId("11111-11111");

        response.setCurrencyConversionFee(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(1000)));
        response.setEstimatedTotalAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(2000)));
        response.setEstimatedInterbankSettlementAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(1300)));

        response.setPsuMessage(SpiMockData.PSU_MESSAGE);
        response.setTppMessages(SpiMockData.TPP_MESSAGES);
        response.setScaMethods(SpiMockData.SCA_METHODS);

        aspspConsentDataProvider.updateAspspConsentData(TEST_ASPSP_DATA.getBytes());
        return SpiResponse.<SpiPeriodicPaymentInitiationResponse>builder()
                   .payload(response)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPeriodicPayment> getPaymentById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#getPaymentById: contextData {}, spiPeriodicPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPeriodicPayment>builder()
                   .payload(payment)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#getPaymentStatusById: contextData {}, spiPeriodicPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .payload(new SpiGetPaymentStatusResponse(payment.getPaymentStatus(), null, SpiGetPaymentStatusResponse.RESPONSE_TYPE_JSON, null, PSU_MESSAGE,
                                                            SpiMockData.SPI_LINKS,
                                                            SpiMockData.TPP_MESSAGES))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#executePaymentWithoutSca: contextData {}, spiPeriodicPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiPeriodicPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#verifyScaAuthorisationAndExecutePayment: contextData {}, spiScaConfirmation{}, spiPeriodicPayment {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .build();
    }

    @Override
    public @NotNull SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("PeriodicPaymentSpi#checkConfirmationCode: contextData {}, spiCheckConfirmationCodeRequest{}, authorisationId {}, aspspConsentData {}", contextData, spiCheckConfirmationCodeRequest.getConfirmationCode(), spiCheckConfirmationCodeRequest.getAuthorisationId(), aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                   .payload(new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FINALISED, TransactionStatus.ACSP))
                   .build();
    }


    @Override
    @NotNull
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull SpiPeriodicPayment payment, boolean isCancellation, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.notifyConfirmationCodeValidation(confirmationCodeValidationResult, payment, isCancellation);
    }
}
