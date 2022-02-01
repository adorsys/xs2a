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
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiGetPaymentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class BulkPaymentSpiMockImpl implements BulkPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String PSU_MESSAGE = "Message from ASPSP to PSU";
    private static final String DEBTOR_NAME = "Mocked debtor name from ASPSP";

    private final PaymentServiceMock paymentService;

    @Override
    @NotNull
    public SpiResponse<SpiBulkPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("BulkPaymentSpi#initiatePayment: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());
        SpiBulkPaymentInitiationResponse response = new SpiBulkPaymentInitiationResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setPaymentId(UUID.randomUUID().toString());
        response.setAspspAccountId("11111-11119");
        List<SpiSinglePayment> payments = new ArrayList<>();
        payments.add(buildSpiSinglePayment(UUID.randomUUID().toString()));
        payments.add(buildSpiSinglePayment(UUID.randomUUID().toString()));
        response.setPayments(payments);

        aspspConsentDataProvider.updateAspspConsentData(TEST_ASPSP_DATA.getBytes());

        response.setCurrencyConversionFee(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(1000)));
        response.setEstimatedTotalAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(2000)));
        response.setEstimatedInterbankSettlementAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(1300)));

        response.setPsuMessage(SpiMockData.PSU_MESSAGE);
        response.setTppMessages(SpiMockData.TPP_MESSAGES);
        response.setScaMethods(SpiMockData.SCA_METHODS);

        return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                   .payload(response)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiBulkPayment> getPaymentById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiBulkPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {

        if (payment.getDebtorName() == null) {
            payment.setDebtorName(DEBTOR_NAME);
        }

        log.info("BulkPaymentSpi#getPaymentById: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiBulkPayment>builder()
                   .payload(payment)
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull String acceptMediaType, @NotNull SpiBulkPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("BulkPaymentSpi#getPaymentStatusById: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiGetPaymentStatusResponse>builder()
                   .payload(new SpiGetPaymentStatusResponse(payment.getPaymentStatus(), true, SpiGetPaymentStatusResponse.RESPONSE_TYPE_JSON, null, PSU_MESSAGE,
                                                            SpiMockData.SPI_LINKS,
                                                            SpiMockData.TPP_MESSAGES))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("BulkPaymentSpi#executePaymentWithoutSca: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiBulkPayment payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("BulkPaymentSpi#verifyScaAuthorisationAndExecutePayment: contextData {}, spiScaConfirmation{}, spiBulkPayment {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .build();
    }

    @Override
    public @NotNull SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData, @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("BulkPaymentSpi#checkConfirmationCode: contextData {}, spiCheckConfirmationCodeRequest{}, authorisationId {}, aspspConsentData {}", contextData, spiCheckConfirmationCodeRequest.getConfirmationCode(), spiCheckConfirmationCodeRequest.getAuthorisationId(), aspspConsentDataProvider.loadAspspConsentData());

        return SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                   .payload(new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FINALISED, TransactionStatus.ACSP))
                   .build();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData contextData, boolean confirmationCodeValidationResult, @NotNull SpiBulkPayment payment, boolean isCancellation, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.notifyConfirmationCodeValidation(confirmationCodeValidationResult, payment, isCancellation);
    }

    private SpiSinglePayment buildSpiSinglePayment(String paymentId) {
        SpiSinglePayment payment = new SpiSinglePayment("sepa-credit-transfers");
        payment.setPaymentId(paymentId);
        payment.setEndToEndIdentification("WBG-123456789");
        payment.setDebtorAccount(SpiAccountReference.builder()
                                     .iban("DE89370400440532013000")
                                     .currency(Currency.getInstance("EUR"))
                                     .build());
        payment.setInstructedAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(50)));
        payment.setCreditorAccount(SpiAccountReference.builder()
                                       .iban("DE52500105173911841934")
                                       .currency(Currency.getInstance("EUR"))
                                       .build());
        payment.setCreditorAgent("FSDFSASGSGF");
        payment.setCreditorName("WBG");
        payment.setCreditorAddress(new SpiAddress("Herrnstraße", "123-34", "Nürnberg", "90431", "DE"));
        payment.setRemittanceInformationUnstructured("Ref. Number WBG-1234");
        payment.setRequestedExecutionDate(LocalDate.of(2020, Month.JANUARY, 1));
        payment.setRequestedExecutionTime(OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        payment.setPaymentStatus(TransactionStatus.RCVD);
        return payment;
    }
}
