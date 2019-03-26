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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentExecutionResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
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
public class BulkPaymentSpiMockImpl implements BulkPaymentSpi {
    private static final String TEST_ASPSP_DATA = "Test aspsp data";

    @Override
    @NotNull
    public SpiResponse<SpiBulkPaymentInitiationResponse> initiatePayment(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData initialAspspConsentData) {
        log.info("BulkPaymentSpi#initiatePayment: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, initialAspspConsentData);
        SpiBulkPaymentInitiationResponse response = new SpiBulkPaymentInitiationResponse();
        response.setTransactionStatus(TransactionStatus.RCVD);
        response.setPaymentId(UUID.randomUUID().toString());
        response.setAspspAccountId("11111-11119");
        List<SpiSinglePayment> payments = new ArrayList<>();
        payments.add(buildSpiSinglePayment(UUID.randomUUID().toString()));
        payments.add(buildSpiSinglePayment(UUID.randomUUID().toString()));
        response.setPayments(payments);

        return SpiResponse.<SpiBulkPaymentInitiationResponse>builder()
                   .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                   .payload(response)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiBulkPayment> getPaymentById(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("BulkPaymentSpi#getPaymentById: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentData);

        return SpiResponse.<SpiBulkPayment>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(payment)
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<TransactionStatus> getPaymentStatusById(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("BulkPaymentSpi#getPaymentStatusById: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentData);

        return SpiResponse.<TransactionStatus>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(payment.getPaymentStatus())
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("BulkPaymentSpi#executePaymentWithoutSca: contextData {}, spiBulkPayment {}, aspspConsentData {}", contextData, payment, aspspConsentData);

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .success();
    }

    @Override
    @NotNull
    public SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData contextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiBulkPayment payment, @NotNull AspspConsentData aspspConsentData) {
        log.info("BulkPaymentSpi#verifyScaAuthorisationAndExecutePayment: contextData {}, spiScaConfirmation{}, spiBulkPayment {}, aspspConsentData {}", contextData, spiScaConfirmation, payment, aspspConsentData);

        return SpiResponse.<SpiPaymentExecutionResponse>builder()
                   .aspspConsentData(aspspConsentData)
                   .payload(new SpiPaymentExecutionResponse(TransactionStatus.ACCP))
                   .success();
    }

    private SpiSinglePayment buildSpiSinglePayment(String paymentId) {
        SpiSinglePayment payment = new SpiSinglePayment("sepa-credit-transfers");
        payment.setPaymentId(paymentId);
        payment.setEndToEndIdentification("WBG-123456789");
        payment.setDebtorAccount(new SpiAccountReference(null, "DE89370400440532013000", null, null, null, null, Currency.getInstance("EUR")));
        payment.setInstructedAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal(50)));
        payment.setCreditorAccount(new SpiAccountReference(null, "DE52500105173911841934", null, null, null, null, Currency.getInstance("EUR")));
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
