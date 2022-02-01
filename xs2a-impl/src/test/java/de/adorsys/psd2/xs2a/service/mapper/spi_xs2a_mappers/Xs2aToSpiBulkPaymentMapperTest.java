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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aToSpiBulkPaymentMapperTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String RESOURCE_ID = "5c2d20da-f20a-4a5e-bf6d-be5b239e3561";
    private static final String IBAN = "DE123456789";
    private static final String DEB_ACCOUNT_ID = "11111_debtorAccount";
    private static final String CRED_ACCOUNT_ID = "2222_creditorAccount";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(REQUESTED_EXECUTION_DATE,
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);
    @InjectMocks
    private Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;

    @BeforeEach
    void setUp() {
        psuDataList.addAll(Arrays.asList(buildPsu(PSU_ID_1), buildPsu(PSU_ID_2)));
        spiPsuDataList.addAll(Arrays.asList(buildSpiPsu(PSU_ID_1), buildSpiPsu(PSU_ID_2)));
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList))
            .thenReturn(spiPsuDataList);
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(buildAccountReference(DEB_ACCOUNT_ID)))
            .thenReturn(buildSpiAccountReference());
    }

    @Test
    void mapToSpiBulkPaymentSuccess() {
        //Given
        BulkPayment payment = buildBulkPayment();
        //When
        SpiBulkPayment spiBulkPayment = xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(payment, PAYMENT_PRODUCT);
        //Then
        assertEquals(PAYMENT_ID, spiBulkPayment.getPaymentId());
        assertTrue(spiBulkPayment.getBatchBookingPreferred());
        assertEquals(buildSpiAccountReference(), spiBulkPayment.getDebtorAccount());
        assertEquals(REQUESTED_EXECUTION_DATE, spiBulkPayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, spiBulkPayment.getRequestedExecutionTime());
        assertEquals(TRANSACTION_STATUS, spiBulkPayment.getPaymentStatus());
        assertFalse(spiBulkPayment.getPayments().isEmpty());
        assertEquals(PAYMENT_PRODUCT, spiBulkPayment.getPaymentProduct());
        assertEquals(spiPsuDataList, spiBulkPayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, spiBulkPayment.getStatusChangeTimestamp());
        assertEquals(payment.getCreationTimestamp(), spiBulkPayment.getCreationTimestamp());
        assertEquals(payment.getContentType(), spiBulkPayment.getContentType());
    }

    @NotNull
    private BulkPayment buildBulkPayment() {
        BulkPayment payment = new BulkPayment();
        payment.setPaymentId(PAYMENT_ID);
        payment.setPayments(Collections.singletonList(buildSinglePayment()));
        payment.setPsuDataList(psuDataList);
        payment.setBatchBookingPreferred(true);
        payment.setDebtorAccount(buildAccountReference(DEB_ACCOUNT_ID));
        payment.setTransactionStatus(TRANSACTION_STATUS);
        payment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        payment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        payment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        payment.setCreationTimestamp(OffsetDateTime.now());
        payment.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return payment;
    }

    private SinglePayment buildSinglePayment() {
        SinglePayment payment = new SinglePayment();
        Xs2aAmount amount = buildXs2aAmount();
        payment.setPaymentId(PAYMENT_ID);
        payment.setInstructedAmount(amount);
        payment.setDebtorAccount(buildAccountReference(DEB_ACCOUNT_ID));
        payment.setCreditorAccount(buildAccountReference(CRED_ACCOUNT_ID));
        payment.setTransactionStatus(TRANSACTION_STATUS);
        return payment;
    }

    private Xs2aAmount buildXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(EUR_CURRENCY);
        amount.setAmount("100");
        return amount;
    }

    private AccountReference buildAccountReference(String accountId) {
        AccountReference reference = new AccountReference();
        reference.setIban(IBAN);
        reference.setCurrency(EUR_CURRENCY);
        reference.setAspspAccountId(accountId);
        reference.setResourceId(RESOURCE_ID);
        return reference;
    }

    private SpiAccountReference buildSpiAccountReference() {
        return SpiAccountReference.builder().resourceId(RESOURCE_ID).iban(IBAN).currency(EUR_CURRENCY).build();
    }

    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return SpiPsuData.builder().psuId(psuId).build();
    }
}
