/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.*;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
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
class Xs2aToSpiPeriodicPaymentMapperTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String END_TO_END_IDENTIFICATION = "PAYMENT_ID";
    private static final String INSTRUCTION_IDENTIFICATION = "INSTRUCTION_IDENTIFICATION";
    private static final String IBAN = "DE123456789";
    private static final String RESOURCE_ID = "5c2d20da-f20a-4a5e-bf6d-be5b239e3561";
    private static final String DEB_ACCOUNT_ID = "11111_debtorAccount";
    private static final String CRED_ACCOUNT_ID = "2222_creditorAccount";
    private static final String CREDITOR_AGENT = "AAAADEBBXXX";
    private static final String CREDITOR_NAME = "WBG";
    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "Ref Number Merchant";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String STREET = "Herrnstraße";
    private static final String BUILDING_NUMBER = "123-34";
    private static final String CITY = "Nürnberg";
    private static final String POSTAL_CODE = "90431";
    private static final String COUNTRY = "Germany";
    private static final String PSU_ID_1 = "First";
    private static final String PSU_ID_2 = "Second";
    private static final LocalDate START_DATE = LocalDate.now().minusDays(1);
    private static final LocalDate END_DATE = LocalDate.now().plusDays(1);
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(LocalDate.now(),
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);
    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final PurposeCode PURPOSE_CODE = PurposeCode.fromValue("BKDF");
    private static final String REMITTANCE = "reference";

    @InjectMocks
    private Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPaymentInfoMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;
    @Mock
    private Xs2aToSpiAddressMapper xs2aToSpiAddressMapper;
    @Mock
    private Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
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
    }

    @Test
    void mapToSpiPeriodicPaymentSuccess() {
        //Given
        PeriodicPayment periodicPayment = buildPeriodicPayment();
        //When
        SpiPeriodicPayment spiPeriodicPayment = xs2aToSpiPaymentInfoMapper.mapToSpiPeriodicPayment(periodicPayment, PAYMENT_PRODUCT);
        //Then
        assertEquals(START_DATE, spiPeriodicPayment.getStartDate());
        assertEquals(END_DATE, spiPeriodicPayment.getEndDate());
        assertEquals(PisExecutionRule.PRECEDING, spiPeriodicPayment.getExecutionRule());
        assertEquals(FrequencyCode.MONTHLY, spiPeriodicPayment.getFrequency());
        assertEquals(PisDayOfExecution._13, spiPeriodicPayment.getDayOfExecution());
        assertEquals(PAYMENT_ID, spiPeriodicPayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, spiPeriodicPayment.getEndToEndIdentification());
        assertEquals(INSTRUCTION_IDENTIFICATION, spiPeriodicPayment.getInstructionIdentification());
        assertEquals(buildSpiAccountReference(), spiPeriodicPayment.getDebtorAccount());
        assertEquals(buildSpiAccountReference(), spiPeriodicPayment.getCreditorAccount());
        assertEquals(buildSpiAmount(EUR_CURRENCY), spiPeriodicPayment.getInstructedAmount());
        assertEquals(CREDITOR_AGENT, spiPeriodicPayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, spiPeriodicPayment.getCreditorName());
        assertEquals(buildSpiAddress(), spiPeriodicPayment.getCreditorAddress());
        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, spiPeriodicPayment.getRemittanceInformationUnstructured());
        assertEquals(TRANSACTION_STATUS, spiPeriodicPayment.getPaymentStatus());
        assertEquals(PAYMENT_PRODUCT, spiPeriodicPayment.getPaymentProduct());
        assertEquals(REQUESTED_EXECUTION_DATE, spiPeriodicPayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, spiPeriodicPayment.getRequestedExecutionTime());
        assertEquals(spiPsuDataList, spiPeriodicPayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, spiPeriodicPayment.getStatusChangeTimestamp());
        assertEquals(ULTIMATE_DEBTOR, spiPeriodicPayment.getUltimateDebtor());
        assertEquals(ULTIMATE_CREDITOR, spiPeriodicPayment.getUltimateCreditor());
        assertEquals(PURPOSE_CODE, spiPeriodicPayment.getPurposeCode());
        assertEquals(REMITTANCE, spiPeriodicPayment.getRemittanceInformationStructured());
        assertEquals(Collections.singletonList(REMITTANCE), spiPeriodicPayment.getRemittanceInformationStructuredArray());
        assertEquals(periodicPayment.getCreationTimestamp(), spiPeriodicPayment.getCreationTimestamp());
        assertEquals(periodicPayment.getContentType(), spiPeriodicPayment.getContentType());
    }

    private PeriodicPayment buildPeriodicPayment() {
        PeriodicPayment periodicPayment = new PeriodicPayment();
        periodicPayment.setStartDate(START_DATE);
        periodicPayment.setEndDate(END_DATE);
        periodicPayment.setFrequency(FrequencyCode.MONTHLY);
        periodicPayment.setExecutionRule(PisExecutionRule.PRECEDING);
        periodicPayment.setDayOfExecution(PisDayOfExecution._13);
        periodicPayment.setPaymentId(PAYMENT_ID);
        periodicPayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        periodicPayment.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        periodicPayment.setDebtorAccount(buildAccountReference(DEB_ACCOUNT_ID));
        periodicPayment.setCreditorAccount(buildAccountReference(CRED_ACCOUNT_ID));
        periodicPayment.setInstructedAmount(buildXs2aAmount(EUR_CURRENCY));
        periodicPayment.setCreditorAgent(CREDITOR_AGENT);
        periodicPayment.setCreditorName(CREDITOR_NAME);
        periodicPayment.setCreditorAddress(buildXs2aAddress());
        periodicPayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        periodicPayment.setTransactionStatus(TRANSACTION_STATUS);
        periodicPayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        periodicPayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        periodicPayment.setPsuDataList(psuDataList);
        periodicPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        periodicPayment.setUltimateDebtor(ULTIMATE_DEBTOR);
        periodicPayment.setUltimateCreditor(ULTIMATE_CREDITOR);
        periodicPayment.setPurposeCode(PURPOSE_CODE);
        periodicPayment.setRemittanceInformationStructured(REMITTANCE);
        periodicPayment.setRemittanceInformationStructuredArray(Collections.singletonList(REMITTANCE));
        periodicPayment.setCreationTimestamp(OffsetDateTime.now());
        periodicPayment.setContentType(MediaType.APPLICATION_JSON_VALUE);
        return periodicPayment;
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
        return SpiAccountReference.builder().resourceId(RESOURCE_ID).iban(IBAN).currency(EUR_CURRENCY).build();
    }

    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return SpiPsuData.builder().psuId(psuId).build();
    }
}
