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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Xs2aToSpiSinglePaymentMapperTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String END_TO_END_IDENTIFICATION = "PAYMENT_ID";
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
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final List<PsuIdData> psuDataList = new ArrayList<>();
    private static final List<SpiPsuData> spiPsuDataList = new ArrayList<>();

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

    @Before
    public void setUp() {
        psuDataList.addAll(Arrays.asList(buildPsu(PSU_ID_1), buildPsu(PSU_ID_2)));
        spiPsuDataList.addAll(Arrays.asList(buildSpiPsu(PSU_ID_1), buildSpiPsu(PSU_ID_2)));
        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(psuDataList))
            .thenReturn(spiPsuDataList);
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(buildAccountReference(DEB_ACCOUNT_ID)))
            .thenReturn(buildSpiAccountReference());
        when(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(buildAccountReference(CRED_ACCOUNT_ID)))
            .thenReturn(buildSpiAccountReference());
        when(xs2aToSpiAmountMapper.mapToSpiAmount(buildXs2aAmount(EUR_CURRENCY, "100")))
            .thenReturn(buildSpiAmount(EUR_CURRENCY, "100"));
        when(xs2aToSpiAddressMapper.mapToSpiAddress(buildXs2aAddress()))
            .thenReturn(buildSpiAddress());
    }

    @Test
    public void mapToSpiSinglePaymentSuccess() {
        //Given
        SinglePayment singlePayment = buildSinglePayment();
        //When
        SpiSinglePayment spiSinglePayment = xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, PAYMENT_PRODUCT);
        //Then
        assertEquals(spiSinglePayment.getPaymentId(), PAYMENT_ID);
        assertEquals(spiSinglePayment.getEndToEndIdentification(), END_TO_END_IDENTIFICATION);
        assertEquals(spiSinglePayment.getDebtorAccount(), buildSpiAccountReference());
        assertEquals(spiSinglePayment.getCreditorAccount(), buildSpiAccountReference());
        assertEquals(spiSinglePayment.getInstructedAmount(), buildSpiAmount(EUR_CURRENCY, "100"));
        assertEquals(spiSinglePayment.getCreditorAgent(), CREDITOR_AGENT);
        assertEquals(spiSinglePayment.getCreditorName(), CREDITOR_NAME);
        assertEquals(spiSinglePayment.getCreditorAddress(), buildSpiAddress());
        assertEquals(spiSinglePayment.getRemittanceInformationUnstructured(), REMITTANCE_INFORMATION_UNSTRUCTURED);
        assertEquals(spiSinglePayment.getPaymentStatus(), TRANSACTION_STATUS);
        assertEquals(spiSinglePayment.getPaymentProduct(), PAYMENT_PRODUCT);
        assertEquals(spiSinglePayment.getRequestedExecutionDate(), REQUESTED_EXECUTION_DATE);
        assertEquals(spiSinglePayment.getRequestedExecutionTime(), REQUESTED_EXECUTION_TIME);
        assertEquals(spiSinglePayment.getPsuDataList(), spiPsuDataList);
    }

    private SinglePayment buildSinglePayment() {
        SinglePayment singlePayment = new SinglePayment();
        singlePayment.setPaymentId(PAYMENT_ID);
        singlePayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        singlePayment.setDebtorAccount(buildAccountReference(DEB_ACCOUNT_ID));
        singlePayment.setCreditorAccount(buildAccountReference(CRED_ACCOUNT_ID));
        singlePayment.setInstructedAmount(buildXs2aAmount(EUR_CURRENCY, "100"));
        singlePayment.setCreditorAgent(CREDITOR_AGENT);
        singlePayment.setCreditorName(CREDITOR_NAME);
        singlePayment.setCreditorAddress(buildXs2aAddress());
        singlePayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        singlePayment.setTransactionStatus(TRANSACTION_STATUS);
        singlePayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        singlePayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        singlePayment.setPsuDataList(psuDataList);
        return singlePayment;
    }

    private SpiAddress buildSpiAddress() {
        return new SpiAddress(STREET, BUILDING_NUMBER, CITY, POSTAL_CODE, COUNTRY);
    }

    private Xs2aAddress buildXs2aAddress() {
        Xs2aAddress xs2aAddress = new Xs2aAddress();
        xs2aAddress.setStreet(STREET);
        xs2aAddress.setBuildingNumber(BUILDING_NUMBER);
        xs2aAddress.setCity(CITY);
        xs2aAddress.setPostalCode(POSTAL_CODE);
        xs2aAddress.setCountry(new Xs2aCountryCode(COUNTRY));
        return xs2aAddress;
    }

    private Xs2aAmount buildXs2aAmount(Currency currency, String sum) {
        return new Xs2aAmount(currency, sum);
    }

    private SpiAmount buildSpiAmount(Currency currency, String sum) {
        return new SpiAmount(currency, new BigDecimal(sum));
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
        return new SpiAccountReference(RESOURCE_ID, IBAN, null, null, null, null, EUR_CURRENCY);
    }


    private PsuIdData buildPsu(String psuId) {
        return new PsuIdData(psuId, null, null, null);
    }

    private SpiPsuData buildSpiPsu(String psuId) {
        return new SpiPsuData(psuId, null, null, null);
    }
}
