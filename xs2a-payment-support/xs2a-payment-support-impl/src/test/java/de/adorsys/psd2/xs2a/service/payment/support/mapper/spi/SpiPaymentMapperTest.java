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

package de.adorsys.psd2.xs2a.service.payment.support.mapper.spi;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.support.mapper.RawToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiPaymentMapperTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGz";
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(2020, 1, 2, 10, 0, 0, 0, ZoneOffset.UTC);
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC);
    private static final String END_TO_END_IDENTIFICATION = "RI-123456789";
    private static final String IBAN = "DE52500105173911841934";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "1111";
    private static final String MASKED_PAN = "23456xxxxxx1234";
    private static final String MSISDN = "0172/1111111";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final SpiAmount INSTRUCTED_AMOUNT = new SpiAmount(CURRENCY, new BigDecimal("1000.00"));
    private static final SpiAddress ADDRESS = new SpiAddress("WBG Straße", "56", "Nürnberg", "90543", "DE");
    private static final String REMITTANCE_INFORMATION = "Ref. Number TELEKOM-1222";

    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    @Mock
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    @Mock
    private RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    @InjectMocks
    private SpiPaymentMapper spiPaymentMapper;

    private final JsonReader jsonReader = new JsonReader();
    private static final SpiPsuData SPI_PSU_DATA = SpiPsuData.builder()
                                                       .psuId("psu Id")
                                                       .psuIdType("psuId Type")
                                                       .psuCorporateId("psu Corporate Id")
                                                       .psuCorporateIdType("psuCorporate Id Type")
                                                       .build();

    @Test
    void mapToSpiSinglePayment() {
        // Given
        SpiPaymentInfo spiPayment = buildSpiPaymentInfo();
        byte[] paymentBody = jsonReader.getBytesFromFile("json/support/mapper/single-payment-initiation.json");
        spiPayment.setPaymentData(paymentBody);

        SinglePayment xs2aSinglePayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-single-payment.json", SinglePayment.class);
        when(rawToXs2aPaymentMapper.mapToSinglePayment(paymentBody)).thenReturn(xs2aSinglePayment);

        SpiSinglePayment baseSpiSinglePayment = buildBaseSpiSinglePayment();
        when(xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(xs2aSinglePayment, PAYMENT_PRODUCT)).thenReturn(baseSpiSinglePayment);

        SpiSinglePayment expectedPayment = buildEnrichedSpiSinglePayment();

        // When
        SpiSinglePayment actualPayment = spiPaymentMapper.mapToSpiSinglePayment(spiPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToSpiPeriodicPayment() {
        // Given
        SpiPaymentInfo spiPayment = buildSpiPaymentInfo();
        byte[] paymentBody = jsonReader.getBytesFromFile("json/support/mapper/periodic-payment-initiation.json");
        spiPayment.setPaymentData(paymentBody);

        PeriodicPayment xs2aPeriodicPayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-periodic-payment.json", PeriodicPayment.class);
        when(rawToXs2aPaymentMapper.mapToPeriodicPayment(paymentBody)).thenReturn(xs2aPeriodicPayment);

        SpiPeriodicPayment baseSpiPeriodicPayment = buildBaseSpiPeriodicPayment();
        when(xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(xs2aPeriodicPayment, PAYMENT_PRODUCT)).thenReturn(baseSpiPeriodicPayment);

        SpiPeriodicPayment expectedPayment = buildEnrichedSpiPeriodicPayment();

        // When
        SpiPeriodicPayment actualPayment = spiPaymentMapper.mapToSpiPeriodicPayment(spiPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    @Test
    void mapToSpiBulkPayment() {
        // Given
        SpiPaymentInfo spiPayment = buildSpiPaymentInfo();
        byte[] paymentBody = jsonReader.getBytesFromFile("json/support/mapper/bulk-payment-initiation.json");
        spiPayment.setPaymentData(paymentBody);

        BulkPayment xs2aBulkPayment = jsonReader.getObjectFromFile("json/support/mapper/raw-xs2a-bulk-payment.json", BulkPayment.class);
        when(rawToXs2aPaymentMapper.mapToBulkPayment(paymentBody)).thenReturn(xs2aBulkPayment);

        SpiBulkPayment baseSpiBulkPayment = buildBaseSpiBulkPayment();
        when(xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(xs2aBulkPayment, PAYMENT_PRODUCT)).thenReturn(baseSpiBulkPayment);

        SpiBulkPayment expectedPayment = buildEnrichedSpiBulkPayment();

        // When
        SpiBulkPayment actualPayment = spiPaymentMapper.mapToSpiBulkPayment(spiPayment);

        // Then
        assertEquals(expectedPayment, actualPayment);
    }

    private SpiPaymentInfo buildSpiPaymentInfo() {
        SpiPaymentInfo spiPayment = new SpiPaymentInfo(PAYMENT_PRODUCT);
        spiPayment.setPaymentId(PAYMENT_ID);
        spiPayment.setPaymentStatus(TransactionStatus.ACSP);
        spiPayment.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        spiPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        spiPayment.setCreationTimestamp(CREATION_TIMESTAMP);
        return spiPayment;
    }

    private SpiSinglePayment buildBaseSpiSinglePayment() {
        SpiSinglePayment singlePayment = new SpiSinglePayment(PAYMENT_PRODUCT);
        singlePayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        singlePayment.setDebtorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));
        singlePayment.setInstructedAmount(INSTRUCTED_AMOUNT);
        singlePayment.setCreditorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));
        singlePayment.setCreditorAgent("BCENECEQ");
        singlePayment.setCreditorName("Telekom");
        singlePayment.setCreditorAddress(ADDRESS);
        singlePayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION);
        return singlePayment;
    }

    private SpiSinglePayment buildEnrichedSpiSinglePayment() {
        SpiSinglePayment singlePayment = buildBaseSpiSinglePayment();
        singlePayment.setPaymentId(PAYMENT_ID);
        singlePayment.setPaymentStatus(TransactionStatus.ACSP);
        singlePayment.setPaymentProduct(PAYMENT_PRODUCT);
        singlePayment.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        singlePayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        singlePayment.setCreationTimestamp(CREATION_TIMESTAMP);
        return singlePayment;
    }

    private SpiPeriodicPayment buildBaseSpiPeriodicPayment() {
        SpiPeriodicPayment periodicPayment = new SpiPeriodicPayment(PAYMENT_PRODUCT);
        periodicPayment.setCreditorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));
        periodicPayment.setCreditorAgent("BCENECEQ");
        periodicPayment.setCreditorName("Telekom");
        periodicPayment.setCreditorAddress(ADDRESS);
        periodicPayment.setDayOfExecution(PisDayOfExecution.DAY_14);
        periodicPayment.setDebtorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));
        periodicPayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        periodicPayment.setExecutionRule(PisExecutionRule.PRECEDING);
        periodicPayment.setFrequency(FrequencyCode.ANNUAL);
        periodicPayment.setInstructedAmount(INSTRUCTED_AMOUNT);
        periodicPayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION);
        periodicPayment.setStartDate(LocalDate.of(2017, 3, 3));
        periodicPayment.setEndDate(LocalDate.of(2020, 12, 2));
        return periodicPayment;
    }

    private SpiPeriodicPayment buildEnrichedSpiPeriodicPayment() {
        SpiPeriodicPayment spiPeriodicPayment = buildBaseSpiPeriodicPayment();
        spiPeriodicPayment.setPaymentId(PAYMENT_ID);
        spiPeriodicPayment.setPaymentStatus(TransactionStatus.ACSP);
        spiPeriodicPayment.setPaymentProduct(PAYMENT_PRODUCT);
        spiPeriodicPayment.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        spiPeriodicPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        spiPeriodicPayment.setCreationTimestamp(CREATION_TIMESTAMP);
        return spiPeriodicPayment;
    }

    private SpiBulkPayment buildBaseSpiBulkPayment() {
        SpiBulkPayment bulkPayment = new SpiBulkPayment();
        bulkPayment.setBatchBookingPreferred(true);
        bulkPayment.setDebtorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));

        SpiSinglePayment bulkPaymentPart = new SpiSinglePayment(PAYMENT_PRODUCT);
        bulkPaymentPart.setInstructedAmount(INSTRUCTED_AMOUNT);
        bulkPaymentPart.setCreditorAccount(new SpiAccountReference(null, null, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, CURRENCY, null));
        bulkPaymentPart.setCreditorAgent("AAAADEBBXXX");
        bulkPaymentPart.setCreditorName("WBG");
        bulkPaymentPart.setCreditorAddress(ADDRESS);
        bulkPaymentPart.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        bulkPaymentPart.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION);
        bulkPaymentPart.setUltimateDebtor("ultimateDebtor");
        bulkPaymentPart.setUltimateCreditor("ultimateCreditor");
        bulkPaymentPart.setPurposeCode(PurposeCode.CDQC);
        bulkPaymentPart.setRemittanceInformationStructured("reference");

        bulkPayment.setPayments(Collections.singletonList(bulkPaymentPart));
        return bulkPayment;
    }

    private SpiBulkPayment buildEnrichedSpiBulkPayment() {
        SpiBulkPayment bulkPayment = buildBaseSpiBulkPayment();
        bulkPayment.setPaymentId(PAYMENT_ID);
        bulkPayment.setPaymentStatus(TransactionStatus.ACSP);
        bulkPayment.setPaymentProduct(PAYMENT_PRODUCT);
        bulkPayment.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        bulkPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        bulkPayment.setCreationTimestamp(CREATION_TIMESTAMP);
        return bulkPayment;
    }
}
