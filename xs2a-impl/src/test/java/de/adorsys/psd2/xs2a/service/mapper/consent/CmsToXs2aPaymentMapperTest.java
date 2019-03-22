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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CmsToXs2aPaymentMapperTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String EXECUTION_ID = "execution id";
    private static final String END_TO_END_IDENTIFICATION = "RI-123456789";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    private static final String DEBTOR_IBAN = "debtor iban";
    private static final AccountReference DEBTOR_ACCOUNT_REFERENCE = new AccountReference(AccountReferenceType.IBAN,
                                                                                          DEBTOR_IBAN,
                                                                                          CURRENCY);

    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String AMOUNT = "100";

    private static final String CREDITOR_IBAN = "creditor iban";
    private static final AccountReference CREDITOR_ACCOUNT_REFERENCE = new AccountReference(AccountReferenceType.IBAN,
                                                                                            CREDITOR_IBAN,
                                                                                            CURRENCY);

    private static final String CREDITOR_AGENT = "creditor agent";
    private static final String CREDITOR_NAME = "creditor name";

    private static final String CREDITOR_ADDRESS_STREET = "street";
    private static final String CREDITOR_ADDRESS_BUILDING_NUMBER = "building number";
    private static final String CREDITOR_ADDRESS_CITY = "city";
    private static final String CREDITOR_ADDRESS_POSTAL_CODE = "postal code";
    private static final String CREDITOR_ADDRESS_COUNTRY = "DE";

    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "remittance information unstructured";
    private static final String REMITTANCE_INFORMATION_STRUCTURED_REFERENCE = "structured reference";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.of(2019, 2, 27);
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.of(REQUESTED_EXECUTION_DATE,
                                                                                     LocalTime.NOON,
                                                                                     ZoneOffset.UTC);
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(REQUESTED_EXECUTION_DATE,
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);

    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final String PURPOSE_CODE = "purpose code";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private static final LocalDate END_DATE = LocalDate.of(2019, 2, 28);
    private static final PisExecutionRule EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final String FREQUENCY = "ANNUAL";
    private static final PisDayOfExecution DAY_OF_EXECUTION = PisDayOfExecution._2;
    private static final List<PsuIdData> PSU_ID_DATA_LIST = Collections.singletonList(new PsuIdData("psu id", null, null, null));

    private static final String PAYMENT_PRODUCT = "payment product";
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final byte[] PAYMENT_DATA = "payment data".getBytes();


    @InjectMocks
    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;

    @Before
    public void setUp() {

    }

    @Test
    public void mapToPeriodicPayment() {
        PisPayment pisPayment = buildPisPayment();

        PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(pisPayment);

        assertNotNull(periodicPayment);
        assertEquals(PAYMENT_ID, periodicPayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, periodicPayment.getEndToEndIdentification());
        assertEquals(DEBTOR_ACCOUNT_REFERENCE, periodicPayment.getDebtorAccount());

        Xs2aAmount instructedAmount = periodicPayment.getInstructedAmount();
        assertNotNull(instructedAmount);
        assertEquals(CURRENCY, instructedAmount.getCurrency());
        assertEquals(AMOUNT, instructedAmount.getAmount());

        assertEquals(CREDITOR_ACCOUNT_REFERENCE, periodicPayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, periodicPayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, periodicPayment.getCreditorName());

        Xs2aAddress creditorAddress = periodicPayment.getCreditorAddress();
        assertNotNull(creditorAddress);
        assertEquals(CREDITOR_ADDRESS_STREET, creditorAddress.getStreet());
        assertEquals(CREDITOR_ADDRESS_BUILDING_NUMBER, creditorAddress.getBuildingNumber());
        assertEquals(CREDITOR_ADDRESS_CITY, creditorAddress.getCity());
        assertEquals(CREDITOR_ADDRESS_POSTAL_CODE, creditorAddress.getPostalCode());
        assertEquals(CREDITOR_ADDRESS_COUNTRY, creditorAddress.getCountry().getCode());

        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, periodicPayment.getRemittanceInformationUnstructured());

        assertEquals(TRANSACTION_STATUS, periodicPayment.getTransactionStatus());
        assertEquals(PSU_ID_DATA_LIST, periodicPayment.getPsuDataList());

        assertEquals(START_DATE, periodicPayment.getStartDate());
        assertEquals(EXECUTION_RULE, periodicPayment.getExecutionRule());
        assertEquals(END_DATE, periodicPayment.getEndDate());

        Xs2aFrequencyCode xs2aFrequencyCode = Xs2aFrequencyCode.valueOf(FREQUENCY);
        assertEquals(xs2aFrequencyCode, periodicPayment.getFrequency());

        assertEquals(DAY_OF_EXECUTION, periodicPayment.getDayOfExecution());
        assertEquals(STATUS_CHANGE_TIMESTAMP, periodicPayment.getStatusChangeTimestamp());
    }

    @Test
    public void mapToPeriodicPayment_shouldNotMapDeprecatedFields() {
        PisPayment pisPayment = buildPisPayment();

        PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(pisPayment);

        assertNull(periodicPayment.getUltimateDebtor());
        assertNull(periodicPayment.getUltimateCreditor());
        assertNull(periodicPayment.getPurposeCode());
        assertNull(periodicPayment.getRemittanceInformationStructured());
    }

    @Test
    public void mapToPeriodicPayment_shouldNotMapRequestedExecutionDateAndTime() {
        PisPayment pisPayment = buildPisPayment();

        PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(pisPayment);

        assertNull(periodicPayment.getRequestedExecutionDate());
        assertNull(periodicPayment.getRequestedExecutionTime());
    }

    @Test
    public void mapToPeriodicPayment_withNullPisPayment_shouldReturnNull() {
        PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(null);

        assertNull(periodicPayment);
    }

    @Test
    public void mapToSinglePayment() {
        PisPayment pisPayment = buildPisPayment();

        SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(pisPayment);

        assertNotNull(singlePayment);
        assertEquals(PAYMENT_ID, singlePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, singlePayment.getEndToEndIdentification());
        assertEquals(DEBTOR_ACCOUNT_REFERENCE, singlePayment.getDebtorAccount());

        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        assertNotNull(instructedAmount);
        assertEquals(CURRENCY, instructedAmount.getCurrency());
        assertEquals(AMOUNT, instructedAmount.getAmount());

        assertEquals(CREDITOR_ACCOUNT_REFERENCE, singlePayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, singlePayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, singlePayment.getCreditorName());

        Xs2aAddress creditorAddress = singlePayment.getCreditorAddress();
        assertNotNull(creditorAddress);
        assertEquals(CREDITOR_ADDRESS_STREET, creditorAddress.getStreet());
        assertEquals(CREDITOR_ADDRESS_BUILDING_NUMBER, creditorAddress.getBuildingNumber());
        assertEquals(CREDITOR_ADDRESS_CITY, creditorAddress.getCity());
        assertEquals(CREDITOR_ADDRESS_POSTAL_CODE, creditorAddress.getPostalCode());
        assertEquals(CREDITOR_ADDRESS_COUNTRY, creditorAddress.getCountry().getCode());

        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, singlePayment.getRemittanceInformationUnstructured());
        assertEquals(REQUESTED_EXECUTION_DATE, singlePayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, singlePayment.getRequestedExecutionTime());
        assertEquals(TRANSACTION_STATUS, singlePayment.getTransactionStatus());
        assertEquals(PSU_ID_DATA_LIST, singlePayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, singlePayment.getStatusChangeTimestamp());
    }

    @Test
    public void mapToSinglePayment_shouldNotMapDeprecatedFields() {
        PisPayment pisPayment = buildPisPayment();

        SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(pisPayment);

        assertNull(singlePayment.getUltimateDebtor());
        assertNull(singlePayment.getUltimateCreditor());
        assertNull(singlePayment.getPurposeCode());
        assertNull(singlePayment.getRemittanceInformationStructured());
    }

    @Test
    public void mapToSinglePayment_withNullPisPayment_shouldReturnNull() {
        SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(null);

        assertNull(singlePayment);
    }

    @Test
    public void mapToBulkPayment() {
        PisPayment pisPayment = buildPisPayment();

        BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(Collections.singletonList(pisPayment));

        assertNotNull(bulkPayment);
        assertEquals(PAYMENT_ID, bulkPayment.getPaymentId());

        assertFalse(bulkPayment.getBatchBookingPreferred());

        assertEquals(DEBTOR_ACCOUNT_REFERENCE, bulkPayment.getDebtorAccount());
        assertEquals(REQUESTED_EXECUTION_DATE, bulkPayment.getRequestedExecutionDate());


        assertEquals(TRANSACTION_STATUS, bulkPayment.getTransactionStatus());
        assertEquals(PSU_ID_DATA_LIST, bulkPayment.getPsuDataList());

        assertEquals(1, bulkPayment.getPayments().size());

        SinglePayment firstPayment = bulkPayment.getPayments().get(0);

        assertNotNull(firstPayment);
        assertEquals(PAYMENT_ID, firstPayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, firstPayment.getEndToEndIdentification());
        assertEquals(DEBTOR_ACCOUNT_REFERENCE, firstPayment.getDebtorAccount());

        Xs2aAmount instructedAmount = firstPayment.getInstructedAmount();
        assertNotNull(instructedAmount);
        assertEquals(CURRENCY, instructedAmount.getCurrency());
        assertEquals(AMOUNT, instructedAmount.getAmount());

        assertEquals(CREDITOR_ACCOUNT_REFERENCE, firstPayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, firstPayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, firstPayment.getCreditorName());

        Xs2aAddress creditorAddress = firstPayment.getCreditorAddress();
        assertNotNull(creditorAddress);
        assertEquals(CREDITOR_ADDRESS_STREET, creditorAddress.getStreet());
        assertEquals(CREDITOR_ADDRESS_BUILDING_NUMBER, creditorAddress.getBuildingNumber());
        assertEquals(CREDITOR_ADDRESS_CITY, creditorAddress.getCity());
        assertEquals(CREDITOR_ADDRESS_POSTAL_CODE, creditorAddress.getPostalCode());
        assertEquals(CREDITOR_ADDRESS_COUNTRY, creditorAddress.getCountry().getCode());

        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, firstPayment.getRemittanceInformationUnstructured());
        assertEquals(REQUESTED_EXECUTION_DATE, firstPayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, firstPayment.getRequestedExecutionTime());
        assertEquals(TRANSACTION_STATUS, firstPayment.getTransactionStatus());
        assertEquals(PSU_ID_DATA_LIST, firstPayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, firstPayment.getStatusChangeTimestamp());
    }

    @Test
    public void mapToBulkPayment_shouldNotMapDeprecatedFieldsInSinglePayment() {
        PisPayment pisPayment = buildPisPayment();

        BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(Collections.singletonList(pisPayment));
        SinglePayment firstPayment = bulkPayment.getPayments().get(0);

        assertNull(firstPayment.getUltimateDebtor());
        assertNull(firstPayment.getUltimateCreditor());
        assertNull(firstPayment.getPurposeCode());
        assertNull(firstPayment.getRemittanceInformationStructured());
    }

    @Test
    public void mapToXs2aCommonPayment() {
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse();

        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisCommonPaymentResponse);

        assertNotNull(commonPayment);
        assertEquals(PAYMENT_ID, commonPayment.getPaymentId());
        assertEquals(PAYMENT_PRODUCT, commonPayment.getPaymentProduct());
        assertEquals(TRANSACTION_STATUS, commonPayment.getTransactionStatus());
        assertEquals(PAYMENT_TYPE, commonPayment.getPaymentType());
        assertEquals(PAYMENT_DATA, commonPayment.getPaymentData());
        assertEquals(TPP_INFO, commonPayment.getTppInfo());
        assertEquals(PSU_ID_DATA_LIST, commonPayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, commonPayment.getStatusChangeTimestamp());
    }

    @Test
    public void mapToXs2aCommonPayment_withNullPisCommonPaymentResponse_shouldReturnNull() {
        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(null);

        assertNull(commonPayment);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("authorisation number");
        tppInfo.setAuthorityId("authority id");
        return tppInfo;
    }

    private PisPayment buildPisPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setPaymentId(PAYMENT_ID);
        pisPayment.setExecutionId(EXECUTION_ID);
        pisPayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        pisPayment.setDebtorAccount(DEBTOR_ACCOUNT_REFERENCE);
        pisPayment.setUltimateDebtor(ULTIMATE_DEBTOR);
        pisPayment.setCurrency(CURRENCY);
        pisPayment.setAmount(new BigDecimal(AMOUNT));
        pisPayment.setCreditorAccount(CREDITOR_ACCOUNT_REFERENCE);
        pisPayment.setCreditorAgent(CREDITOR_AGENT);
        pisPayment.setCreditorName(CREDITOR_NAME);
        pisPayment.setCreditorAddress(buildCmsAddress());
        pisPayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        pisPayment.setRemittanceInformationStructured(buildCmsRemittance());
        pisPayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        pisPayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        pisPayment.setUltimateCreditor(ULTIMATE_CREDITOR);
        pisPayment.setPurposeCode(PURPOSE_CODE);
        pisPayment.setTransactionStatus(TRANSACTION_STATUS);
        pisPayment.setStartDate(START_DATE);
        pisPayment.setEndDate(END_DATE);
        pisPayment.setExecutionRule(EXECUTION_RULE);
        pisPayment.setFrequency(FREQUENCY);
        pisPayment.setDayOfExecution(DAY_OF_EXECUTION);
        pisPayment.setPsuDataList(PSU_ID_DATA_LIST);
        pisPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        return pisPayment;
    }

    private CmsAddress buildCmsAddress() {
        CmsAddress cmsAddress = new CmsAddress();
        cmsAddress.setStreet(CREDITOR_ADDRESS_STREET);
        cmsAddress.setBuildingNumber(CREDITOR_ADDRESS_BUILDING_NUMBER);
        cmsAddress.setCity(CREDITOR_ADDRESS_CITY);
        cmsAddress.setPostalCode(CREDITOR_ADDRESS_POSTAL_CODE);
        cmsAddress.setCountry(CREDITOR_ADDRESS_COUNTRY);
        return cmsAddress;
    }

    private CmsRemittance buildCmsRemittance() {
        CmsRemittance cmsRemittance = new CmsRemittance();
        cmsRemittance.setReference(REMITTANCE_INFORMATION_STRUCTURED_REFERENCE);
        return cmsRemittance;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();

        pisCommonPaymentResponse.setPayments(Collections.singletonList(buildPisPayment()));
        pisCommonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PAYMENT_TYPE);
        pisCommonPaymentResponse.setTppInfo(TPP_INFO);
        pisCommonPaymentResponse.setExternalId(PAYMENT_ID);
        pisCommonPaymentResponse.setPsuData(PSU_ID_DATA_LIST);
        pisCommonPaymentResponse.setPaymentData(PAYMENT_DATA);
        pisCommonPaymentResponse.setTransactionStatus(TRANSACTION_STATUS);
        pisCommonPaymentResponse.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);

        return pisCommonPaymentResponse;
    }
}
