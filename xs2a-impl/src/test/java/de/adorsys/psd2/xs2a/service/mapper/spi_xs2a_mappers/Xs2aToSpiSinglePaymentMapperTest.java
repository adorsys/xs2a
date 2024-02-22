/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aToSpiSinglePaymentMapperTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String END_TO_END_IDENTIFICATION = "PAYMENT_ID";
    private static final String INSTRUCTION_IDENTIFICATION = "INSTRUCTION_IDENTIFICATION";
    private static final String IBAN = "DE123456789";
    private static final String RESOURCE_ID = "5c2d20da-f20a-4a5e-bf6d-be5b239e3561";
    private static final String DEB_ACCOUNT_ID = "11111_debtorAccount";
    private static final String CRED_ACCOUNT_ID = "2222_creditorAccount";
    private static final String CREDITOR_AGENT = "AAAADEBBXXX";
    private static final String CREDITOR_NAME = "WBG";
    private static final List<String> REMITTANCE_INFORMATION_UNSTRUCTURED_ARRAY = Collections.singletonList("Ref Number Merchant");
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String STREET = "Herrnstraße";
    private static final String BUILDING_NUMBER = "123-34";
    private static final String CITY = "Nürnberg";
    private static final String POSTAL_CODE = "90431";
    private static final String COUNTRY = "Germany";
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final SpiTransactionStatus SPI_TRANSACTION_STATUS = SpiTransactionStatus.RCVD;
    private final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(LocalDate.now(),
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);
    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final PurposeCode PURPOSE_CODE = PurposeCode.fromValue("BKDF");
    private static final SpiPisPurposeCode SPI_PURPOSE_CODE = SpiPisPurposeCode.fromValue("BKDF");
    private final Remittance REMITTANCE = getRemittance();
    private final SpiRemittance SPI_REMITTANCE = getSpiRemittance();
    private final String REFERENCE = "reference";

    @InjectMocks
    private Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    @Mock
    private Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;
    @Mock
    private Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    @Mock
    private Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private Xs2aToSpiPisMapper xs2aToSpiPisMapper;
    @Mock
    private Xs2aToSpiTransactionMapper xs2aToSpiTransactionMapper;
    @Spy
    private final RemittanceMapper remittanceMapper = Mappers.getMapper(RemittanceMapper.class);

    @BeforeEach
    void setUp() {
        psuDataList.addAll(Arrays.asList(buildPsu(PSU_ID_1), buildPsu(PSU_ID_2)));
        spiPsuDataList.addAll(Arrays.asList(buildSpiPsu(PSU_ID_1), buildSpiPsu(PSU_ID_2)));
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList))
            .thenReturn(spiPsuDataList);
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(buildAccountReference(DEB_ACCOUNT_ID)))
            .thenReturn(buildSpiAccountReference());
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(buildAccountReference(CRED_ACCOUNT_ID)))
            .thenReturn(buildSpiAccountReference());
        when(xs2aToSpiAmountMapper.mapToSpiAmount(buildXs2aAmount(EUR_CURRENCY)))
            .thenReturn(buildSpiAmount(EUR_CURRENCY));
        when(xs2aToSpiAddressMapper.mapToSpiAddress(buildXs2aAddress()))
            .thenReturn(buildSpiAddress());
        when(xs2aToSpiTransactionMapper.mapToSpiTransactionStatus(TRANSACTION_STATUS)).thenReturn(SPI_TRANSACTION_STATUS);
        when(xs2aToSpiPisMapper.mapToSpiPisPurposeCode(PURPOSE_CODE)).thenReturn(SPI_PURPOSE_CODE);

    }

    @Test
    void mapToSpiSinglePaymentSuccess() {
        //Given
        SinglePayment singlePayment = buildSinglePayment();
        //When
        SpiSinglePayment spiSinglePayment = xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, PAYMENT_PRODUCT);
        //Then
        assertEquals(PAYMENT_ID, spiSinglePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, spiSinglePayment.getEndToEndIdentification());
        assertEquals(INSTRUCTION_IDENTIFICATION, spiSinglePayment.getInstructionIdentification());
        assertEquals(buildSpiAccountReference(), spiSinglePayment.getDebtorAccount());
        assertEquals(buildSpiAccountReference(), spiSinglePayment.getCreditorAccount());
        assertEquals(buildSpiAmount(EUR_CURRENCY), spiSinglePayment.getInstructedAmount());
        assertEquals(CREDITOR_AGENT, spiSinglePayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, spiSinglePayment.getCreditorName());
        assertEquals(buildSpiAddress(), spiSinglePayment.getCreditorAddress());
        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED_ARRAY, spiSinglePayment.getRemittanceInformationUnstructuredArray());
        assertEquals(SPI_TRANSACTION_STATUS, spiSinglePayment.getPaymentStatus());
        assertEquals(PAYMENT_PRODUCT, spiSinglePayment.getPaymentProduct());
        assertEquals(REQUESTED_EXECUTION_DATE, spiSinglePayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, spiSinglePayment.getRequestedExecutionTime());
        assertEquals(spiPsuDataList, spiSinglePayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, singlePayment.getStatusChangeTimestamp());
        assertEquals(ULTIMATE_DEBTOR, spiSinglePayment.getUltimateDebtor());
        assertEquals(ULTIMATE_CREDITOR, spiSinglePayment.getUltimateCreditor());
        assertEquals(SPI_PURPOSE_CODE, spiSinglePayment.getPurposeCode());
        assertEquals(Collections.singletonList(SPI_REMITTANCE), spiSinglePayment.getRemittanceInformationStructuredArray());
        assertEquals(singlePayment.getCreationTimestamp(), spiSinglePayment.getCreationTimestamp());
        assertEquals(singlePayment.getContentType(), spiSinglePayment.getContentType());
    }

    private SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setPaymentId(PAYMENT_ID);
        singlePayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        singlePayment.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        singlePayment.setDebtorAccount(buildAccountReference(DEB_ACCOUNT_ID));
        singlePayment.setCreditorAccount(buildAccountReference(CRED_ACCOUNT_ID));
        singlePayment.setInstructedAmount(buildXs2aAmount(EUR_CURRENCY));
        singlePayment.setCreditorAgent(CREDITOR_AGENT);
        singlePayment.setCreditorName(CREDITOR_NAME);
        singlePayment.setCreditorAddress(buildXs2aAddress());
        singlePayment.setRemittanceInformationUnstructuredArray(REMITTANCE_INFORMATION_UNSTRUCTURED_ARRAY);
        singlePayment.setTransactionStatus(TRANSACTION_STATUS);
        singlePayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        singlePayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        singlePayment.setPsuDataList(psuDataList);
        singlePayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        singlePayment.setUltimateDebtor(ULTIMATE_DEBTOR);
        singlePayment.setUltimateCreditor(ULTIMATE_CREDITOR);
        singlePayment.setPurposeCode(PURPOSE_CODE);
        singlePayment.setRemittanceInformationStructuredArray(Collections.singletonList(REMITTANCE));
        singlePayment.setCreationTimestamp(OffsetDateTime.now());
        singlePayment.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return singlePayment;
    }

    private SpiAddress buildSpiAddress() {
        return new SpiAddress(STREET, BUILDING_NUMBER, CITY, POSTAL_CODE, COUNTRY);
    }

    private Xs2aAddress buildXs2aAddress() {
        Xs2aAddress xs2aAddress = new Xs2aAddress();
        xs2aAddress.setStreetName(STREET);
        xs2aAddress.setBuildingNumber(BUILDING_NUMBER);
        xs2aAddress.setTownName(CITY);
        xs2aAddress.setPostCode(POSTAL_CODE);
        xs2aAddress.setCountry(new Xs2aCountryCode(COUNTRY));
        return xs2aAddress;
    }

    private Xs2aAmount buildXs2aAmount(Currency currency) {
        return new Xs2aAmount(currency, "100");
    }

    private SpiAmount buildSpiAmount(Currency currency) {
        return new SpiAmount(currency, new BigDecimal("100"));
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
        return SpiAccountReference.builder()
                   .resourceId(RESOURCE_ID)
                   .iban(IBAN)
                   .currency(EUR_CURRENCY)
                   .build();
    }

    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return SpiPsuData.builder()
                   .psuId(psuId)
                   .build();
    }

    private Remittance getRemittance() {
        Remittance remittance = new Remittance();
        remittance.setReference(REFERENCE);
        remittance.setReferenceType(null);
        remittance.setReferenceIssuer(null);
        return remittance;
    }

    private SpiRemittance getSpiRemittance() {
        SpiRemittance remittance = new SpiRemittance();
        remittance.setReference(REFERENCE);
        remittance.setReferenceType(null);
        remittance.setReferenceIssuer(null);
        return remittance;
    }
}
