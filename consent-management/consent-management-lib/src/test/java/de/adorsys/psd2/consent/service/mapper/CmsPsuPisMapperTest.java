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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAddress;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisRemittance;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CmsPsuPisMapperTest {
    private static final String PAYMENT_PRODUCT = "PAYMENT_PRODUCT";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final PaymentType PAYMENT_TYPE_SINGLE = PaymentType.SINGLE;
    private static final PaymentType PAYMENT_TYPE_PERIODIC = PaymentType.PERIODIC;
    private static final PaymentType PAYMENT_TYPE_BULK = PaymentType.BULK;
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final byte[] PAYMENT_DATA = "PAYMENT_DATA".getBytes();
    private static final String TPP_AUTHORISATION_NUMBER = "TPP_AUTHORISATION_NUMBER";
    private static final String TPP_AUTHORITY_ID = "TPP_AUTHORITY_ID";
    private static final List<TppRole> TPP_ROLES = buildTppRoles();
    private static final TppInfoEntity TPP_INFO_ENTITY = buildTppInfoEntity();
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String PSU_ID = "PSU_ID";
    private static final String PSU_ID_TYPE = "PSU_ID_TYPE";
    private static final String PSU_CORPORATE_ID = "PSU_CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU_CORPORATE_ID_TYPE";
    private static final String PSU_IP_ADDRESS = "PSU_IP_ADDRESS";
    private static final PsuData PSU_DATA = buildPsuData();
    private static final PsuIdData PSU_ID_DATA = buildPsuIdData();
    private static final List<PsuData> PSU_DATA_LIST = Collections.singletonList(PSU_DATA);
    private static final List<PsuIdData> PSU_ID_DATA_LIST = Collections.singletonList(PSU_ID_DATA);
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.now();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.now();
    private static final PisCommonPaymentData PIS_COMMON_PAYMENT_DATA_SINGLE = buildPisCommonPaymentData(PAYMENT_TYPE_SINGLE);
    private static final PisCommonPaymentData PIS_COMMON_PAYMENT_DATA_PERIODIC = buildPisCommonPaymentData(PAYMENT_TYPE_PERIODIC);
    private static final PisCommonPaymentData PIS_COMMON_PAYMENT_DATA_BULK = buildPisCommonPaymentData(PAYMENT_TYPE_BULK);
    private static final String END_TO_END_IDENTIFICATION = "END_TO_END_IDENTIFICATION";
    private static final String INSTRUCTION_IDENTIFICATION = "INSTRUCTION_IDENTIFICATION";
    private static final AccountReferenceEntity DEBTOR_ACCOUNT = buildAccountReferenceEntity();
    private static final AccountReferenceEntity CREDITOR_ACCOUNT = buildAccountReferenceEntity();
    private static final AccountReference DEBTOR_CMS_ACCOUNT = buildAccountReference();
    private static final AccountReference CREDITOR_CMS_ACCOUNT = buildAccountReference();
    private static final String IBAN = "IBAN";
    private static final Currency CURRENCY = Currency.getInstance(Locale.GERMANY);
    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final String CREDITOR_AGENT = "CREDITOR_AGENT";
    private static final String CREDITOR_NAME = "CREDITOR_NAME";
    private static final JsonReader jsonReader = new JsonReader();
    private static final PisAddress CREDITOR_ADDRESS = jsonReader.getObjectFromFile("json/service/mapper/pis-address.json", PisAddress.class);
    private static final CmsAddress CREDITOR_CMS_ADDRESS = jsonReader.getObjectFromFile("json/service/mapper/cms-address.json", CmsAddress.class);
    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "REMITTANCE_INFORMATION_UNSTRUCTURED";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final PisDayOfExecution DAY_OF_EXECUTION = PisDayOfExecution.DAY_1;
    private static final LocalDate START_DATE = LocalDate.now().minus(10, ChronoUnit.DAYS);
    private static final LocalDate END_DATE = LocalDate.now();
    private static final PisExecutionRule EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final String FREEQUENCY = "DAILY";
    private static final PisRemittance REMITTANCE = jsonReader.getObjectFromFile("json/remittance.json", PisRemittance.class);
    private static final PisPaymentData PIS_PAYMENT_DATA_SINGLE = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_SINGLE);
    private static final PisPaymentData PIS_PAYMENT_DATA_PERIODIC = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_PERIODIC);
    private static final PisPaymentData PIS_PAYMENT_DATA_BULK = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_BULK);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_SINGLE_LIST = Collections.singletonList(PIS_PAYMENT_DATA_SINGLE);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_PERIODIC_LIST = Collections.singletonList(PIS_PAYMENT_DATA_PERIODIC);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_BULK_LIST = Collections.singletonList(PIS_PAYMENT_DATA_BULK);
    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final String PURPOSE_CODE = "BKDF";

    @InjectMocks
    private CmsPsuPisMapper cmsPsuPisMapper;

    @Mock
    private CmsAddressMapper cmsAddressMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Spy
    private final CmsRemittanceMapper cmsRemittanceMapper = Mappers.getMapper(CmsRemittanceMapper.class);

    @BeforeEach
    void setUp() {
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY)).thenReturn(TPP_INFO);
        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST)).thenReturn(PSU_ID_DATA_LIST);
    }

    @Test
    void mapToCmsPayment_paymentData_Success() {
        CmsBasePaymentResponse cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_COMMON_PAYMENT_DATA_SINGLE);

        assertNotNull(cmsPayment);
        assertEquals(PAYMENT_PRODUCT, cmsPayment.getPaymentProduct());
        assertEquals(PAYMENT_TYPE_SINGLE, cmsPayment.getPaymentType());
        assertEquals(PAYMENT_ID, cmsPayment.getPaymentId());
        assertEquals(PSU_ID_DATA_LIST, cmsPayment.getPsuIdDatas());
        assertEquals(TPP_INFO, cmsPayment.getTppInfo());
        assertEquals(CREATION_TIMESTAMP, cmsPayment.getCreationTimestamp());
        assertEquals(STATUS_CHANGE_TIMESTAMP, cmsPayment.getStatusChangeTimestamp());
    }

    @Test
    void mapToCmsPayment_pisPaymentDataList_single_Success() {
        when(cmsAddressMapper.mapToCmsAddress(CREDITOR_ADDRESS)).thenReturn(CREDITOR_CMS_ADDRESS);

        CmsBasePaymentResponse cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_SINGLE_LIST);

        assertNotNull(cmsPayment);
        assertTrue(cmsPayment instanceof CmsSinglePayment);

        CmsSinglePayment singlePayment = (CmsSinglePayment) cmsPayment;
        assertEquals(PAYMENT_ID, singlePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, singlePayment.getEndToEndIdentification());
        assertEquals(INSTRUCTION_IDENTIFICATION, singlePayment.getInstructionIdentification());
        assertEquals(DEBTOR_CMS_ACCOUNT, singlePayment.getDebtorAccount());

        CmsAmount instructedAmount = singlePayment.getInstructedAmount();
        assertNotNull(instructedAmount);
        assertEquals(AMOUNT, instructedAmount.getAmount());
        assertEquals(CURRENCY, instructedAmount.getCurrency());

        assertEquals(CREDITOR_CMS_ACCOUNT, singlePayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, singlePayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, singlePayment.getCreditorName());
        assertEquals(CREDITOR_CMS_ADDRESS, singlePayment.getCreditorAddress());
        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, singlePayment.getRemittanceInformationUnstructured());
        assertEquals(REQUESTED_EXECUTION_DATE, singlePayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, singlePayment.getRequestedExecutionTime());
        assertEquals(TPP_INFO, singlePayment.getTppInfo());
        assertEquals(PSU_ID_DATA_LIST, singlePayment.getPsuIdDatas());
        assertEquals(CREATION_TIMESTAMP, singlePayment.getCreationTimestamp());
        assertEquals(STATUS_CHANGE_TIMESTAMP, singlePayment.getStatusChangeTimestamp());
        assertEquals(ULTIMATE_DEBTOR, singlePayment.getUltimateDebtor());
        assertEquals(ULTIMATE_CREDITOR, singlePayment.getUltimateCreditor());
        assertEquals(PURPOSE_CODE, singlePayment.getPurposeCode());
        assertEquals(cmsRemittanceMapper.mapToCmsRemittance(REMITTANCE), singlePayment.getRemittanceInformationStructured());
    }

    @Test
    void mapToCmsPayment_pisPaymentDataList_periodic_Success() {
        when(cmsAddressMapper.mapToCmsAddress(CREDITOR_ADDRESS)).thenReturn(CREDITOR_CMS_ADDRESS);

        CmsBasePaymentResponse cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_PERIODIC_LIST);

        assertNotNull(cmsPayment);
        assertTrue(cmsPayment instanceof CmsPeriodicPayment);

        CmsPeriodicPayment periodicPayment = (CmsPeriodicPayment) cmsPayment;
        assertEquals(PAYMENT_ID, periodicPayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, periodicPayment.getEndToEndIdentification());
        assertEquals(INSTRUCTION_IDENTIFICATION, periodicPayment.getInstructionIdentification());
        assertEquals(DEBTOR_CMS_ACCOUNT, periodicPayment.getDebtorAccount());

        CmsAmount instructedAmount = periodicPayment.getInstructedAmount();
        assertNotNull(instructedAmount);
        assertEquals(AMOUNT, instructedAmount.getAmount());
        assertEquals(CURRENCY, instructedAmount.getCurrency());

        assertEquals(CREDITOR_CMS_ACCOUNT, periodicPayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, periodicPayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, periodicPayment.getCreditorName());
        assertEquals(CREDITOR_CMS_ADDRESS, periodicPayment.getCreditorAddress());
        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, periodicPayment.getRemittanceInformationUnstructured());
        assertEquals(REQUESTED_EXECUTION_DATE, periodicPayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, periodicPayment.getRequestedExecutionTime());
        assertEquals(TPP_INFO, periodicPayment.getTppInfo());
        assertEquals(PSU_ID_DATA_LIST, periodicPayment.getPsuIdDatas());
        assertEquals(CREATION_TIMESTAMP, periodicPayment.getCreationTimestamp());
        assertEquals(STATUS_CHANGE_TIMESTAMP, periodicPayment.getStatusChangeTimestamp());
        assertEquals(ULTIMATE_DEBTOR, periodicPayment.getUltimateDebtor());
        assertEquals(ULTIMATE_CREDITOR, periodicPayment.getUltimateCreditor());
        assertEquals(PURPOSE_CODE, periodicPayment.getPurposeCode());
        assertEquals(cmsRemittanceMapper.mapToCmsRemittance(REMITTANCE), periodicPayment.getRemittanceInformationStructured());
    }

    @Test
    void mapToCmsPayment_pisPaymentDataList_bulk_Success() {
        when(cmsAddressMapper.mapToCmsAddress(CREDITOR_ADDRESS)).thenReturn(CREDITOR_CMS_ADDRESS);

        CmsBasePaymentResponse cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_BULK_LIST);

        assertTrue(cmsPayment instanceof CmsBulkPayment);

        CmsBulkPayment bulkPayment = (CmsBulkPayment) cmsPayment;
        assertEquals(PAYMENT_ID, bulkPayment.getPaymentId());
        assertEquals(DEBTOR_CMS_ACCOUNT, bulkPayment.getDebtorAccount());
        assertFalse(bulkPayment.isBatchBookingPreferred());

        List<CmsSinglePayment> payments = bulkPayment.getPayments();
        CmsSinglePayment singlePayment = payments.get(0);
        assertEquals(PAYMENT_ID, singlePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, singlePayment.getEndToEndIdentification());
        assertEquals(INSTRUCTION_IDENTIFICATION, singlePayment.getInstructionIdentification());
        assertEquals(DEBTOR_CMS_ACCOUNT, singlePayment.getDebtorAccount());

        CmsAmount instructedAmount = singlePayment.getInstructedAmount();
        assertEquals(AMOUNT, instructedAmount.getAmount());
        assertEquals(CURRENCY, instructedAmount.getCurrency());

        assertEquals(CREDITOR_CMS_ACCOUNT, singlePayment.getCreditorAccount());
        assertEquals(CREDITOR_AGENT, singlePayment.getCreditorAgent());
        assertEquals(CREDITOR_NAME, singlePayment.getCreditorName());
        assertEquals(CREDITOR_CMS_ADDRESS, singlePayment.getCreditorAddress());
        assertEquals(REMITTANCE_INFORMATION_UNSTRUCTURED, singlePayment.getRemittanceInformationUnstructured());
        assertEquals(REQUESTED_EXECUTION_DATE, singlePayment.getRequestedExecutionDate());
        assertEquals(REQUESTED_EXECUTION_TIME, singlePayment.getRequestedExecutionTime());
        assertEquals(TPP_INFO, singlePayment.getTppInfo());
        assertEquals(PSU_ID_DATA_LIST, singlePayment.getPsuIdDatas());
        assertEquals(CREATION_TIMESTAMP, singlePayment.getCreationTimestamp());
        assertEquals(STATUS_CHANGE_TIMESTAMP, singlePayment.getStatusChangeTimestamp());
        assertEquals(ULTIMATE_DEBTOR, singlePayment.getUltimateDebtor());
        assertEquals(ULTIMATE_CREDITOR, singlePayment.getUltimateCreditor());
        assertEquals(PURPOSE_CODE, singlePayment.getPurposeCode());
        assertEquals(cmsRemittanceMapper.mapToCmsRemittance(REMITTANCE), singlePayment.getRemittanceInformationStructured());
    }

    private static PisCommonPaymentData buildPisCommonPaymentData(PaymentType paymentType) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setPaymentType(paymentType);
        pisCommonPaymentData.setTransactionStatus(TRANSACTION_STATUS);
        pisCommonPaymentData.setPayment(PAYMENT_DATA);
        pisCommonPaymentData.setTppInfo(TPP_INFO_ENTITY);
        pisCommonPaymentData.setPsuDataList(PSU_DATA_LIST);
        pisCommonPaymentData.setCreationTimestamp(CREATION_TIMESTAMP);
        pisCommonPaymentData.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        return pisCommonPaymentData;
    }

    private static TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfoEntity.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfoEntity.setTppRoles(TPP_ROLES);
        return tppInfoEntity;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfo.setTppRoles(TPP_ROLES);
        return tppInfo;
    }

    private static PsuData buildPsuData() {
        PsuData psuData = new PsuData();
        psuData.setPsuId(PSU_ID);
        psuData.setPsuIdType(PSU_ID_TYPE);
        psuData.setPsuCorporateId(PSU_CORPORATE_ID);
        psuData.setPsuCorporateIdType(PSU_CORPORATE_ID_TYPE);
        return psuData;
    }

    private static PisPaymentData buildPisPaymentData(PisCommonPaymentData pisCommonPaymentData) {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        pisPaymentData.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        pisPaymentData.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setDebtorAccount(DEBTOR_ACCOUNT);
        pisPaymentData.setCurrency(CURRENCY);
        pisPaymentData.setAmount(AMOUNT);
        pisPaymentData.setCreditorAccount(CREDITOR_ACCOUNT);
        pisPaymentData.setCreditorAgent(CREDITOR_AGENT);
        pisPaymentData.setCreditorName(CREDITOR_NAME);
        pisPaymentData.setCreditorAddress(CREDITOR_ADDRESS);
        pisPaymentData.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        pisPaymentData.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        pisPaymentData.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        pisPaymentData.setDayOfExecution(DAY_OF_EXECUTION);
        pisPaymentData.setStartDate(START_DATE);
        pisPaymentData.setEndDate(END_DATE);
        pisPaymentData.setExecutionRule(EXECUTION_RULE);
        pisPaymentData.setFrequency(FREEQUENCY);
        pisPaymentData.setUltimateDebtor(ULTIMATE_DEBTOR);
        pisPaymentData.setUltimateCreditor(ULTIMATE_CREDITOR);
        pisPaymentData.setPurposeCode(PURPOSE_CODE);
        pisPaymentData.setRemittanceInformationStructured(REMITTANCE);
        return pisPaymentData;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    }

    private static List<TppRole> buildTppRoles() {
        return Arrays.asList(TppRole.AISP, TppRole.ASPSP, TppRole.PIISP, TppRole.PISP);
    }

    private static AccountReferenceEntity buildAccountReferenceEntity() {
        AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
        accountReferenceEntity.setIban(IBAN);
        return accountReferenceEntity;
    }

    private static AccountReference buildAccountReference() {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(IBAN);
        return accountReference;
    }
}
