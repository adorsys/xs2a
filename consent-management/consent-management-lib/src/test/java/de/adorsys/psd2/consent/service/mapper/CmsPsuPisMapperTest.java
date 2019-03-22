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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAddress;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisMapperTest {
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
    private static final String REDIRECT_URI = "REDIRECT_URI";
    private static final String NOK_REDIRECT_URI = "NOK_REDIRECT_URI";
    private static final TppInfoEntity TPP_INFO_ENTITY = buildTppInfoEntity();
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final String PSU_ID = "PSU_ID";
    private static final String PSU_ID_TYPE = "PSU_ID_TYPE";
    private static final String PSU_CORPORATE_ID = "PSU_CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "PSU_CORPORATE_ID_TYPE";
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
    private static final AccountReferenceEntity DEBTOR_ACCOUNT = buildAccountReferenceEntity();
    private static final AccountReferenceEntity CREDITOR_ACCOUNT = buildAccountReferenceEntity();
    private static final CmsAccountReference DEBTOR_CMS_ACCOUNT = buildCmsAccountReference();
    private static final CmsAccountReference CREDITOR_CMS_ACCOUNT = buildCmsAccountReference();
    private static final String IBAN = "IBAN";
    private static final Currency CURRENCY = Currency.getInstance(Locale.GERMANY);
    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final String CREDITOR_AGENT = "CREDITOR_AGENT";
    private static final String CREDITOR_NAME = "CREDITOR_NAME";
    private static final long PSU_ADDRESS_ID = 123456789L;
    private static final String COUNTRY = "COUNTRY";
    private static final String CITY = "CITY";
    private static final String STREET = "STREET";
    private static final String BUILDING_NUMBER = "BUILDING_NUMBER";
    private static final String POSTAL_CODE = "POSTAL_CODE";
    private static final PisAddress CREDITOR_ADDRESS = buildPisAddress();
    private static final CmsAddress CREDITOR_CMS_ADDRESS = buildCmsAddress();
    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "REMITTANCE_INFORMATION_UNSTRUCTURED";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.now();
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.now();
    private static final PisDayOfExecution DAY_OF_EXECUTION = PisDayOfExecution._1;
    private static final LocalDate START_DATE = LocalDate.now().minus(10, ChronoUnit.DAYS);
    private static final LocalDate END_DATE = LocalDate.now();
    private static final PisExecutionRule EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final String FREEQUENCY = "DAILY";
    private static final PisPaymentData PIS_PAYMENT_DATA_SINGLE = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_SINGLE);
    private static final PisPaymentData PIS_PAYMENT_DATA_PERIODIC = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_PERIODIC);
    private static final PisPaymentData PIS_PAYMENT_DATA_BULK = buildPisPaymentData(PIS_COMMON_PAYMENT_DATA_BULK);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_SINGLE_LIST = Collections.singletonList(PIS_PAYMENT_DATA_SINGLE);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_PERIODIC_LIST = Collections.singletonList(PIS_PAYMENT_DATA_PERIODIC);
    private static final List<PisPaymentData> PIS_PAYMENT_DATA_BULK_LIST = Collections.singletonList(PIS_PAYMENT_DATA_BULK);

    @InjectMocks
    private CmsPsuPisMapper cmsPsuPisMapper;

    @Mock
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private PsuDataMapper psuDataMapper;

    @Before
    public void setUp() {
        when(tppInfoMapper.mapToTppInfo(TPP_INFO_ENTITY))
            .thenReturn(TPP_INFO);

        when(psuDataMapper.mapToPsuIdDataList(PSU_DATA_LIST))
            .thenReturn(PSU_ID_DATA_LIST);

        when(pisCommonPaymentMapper.mapToCmsAddress(CREDITOR_ADDRESS))
            .thenReturn(CREDITOR_CMS_ADDRESS);
    }

    @Test
    public void mapToCmsPayment_paymentData_Success() {
        CmsPayment cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_COMMON_PAYMENT_DATA_SINGLE);

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
    public void mapToCmsPayment_pisPaymentDataList_single_Success() {
        CmsPayment cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_SINGLE_LIST);

        assertNotNull(cmsPayment);
        assertTrue(cmsPayment instanceof CmsSinglePayment);

        CmsSinglePayment singlePayment = (CmsSinglePayment) cmsPayment;
        assertEquals(PAYMENT_ID, singlePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, singlePayment.getEndToEndIdentification());
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
    }

    @Test
    public void mapToCmsPayment_pisPaymentDataList_periodic_Success() {
        CmsPayment cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_PERIODIC_LIST);

        assertNotNull(cmsPayment);
        assertTrue(cmsPayment instanceof CmsPeriodicPayment);

        CmsPeriodicPayment periodicPayment = (CmsPeriodicPayment) cmsPayment;
        assertEquals(PAYMENT_ID, periodicPayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, periodicPayment.getEndToEndIdentification());
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
    }

    @Test
    public void mapToCmsPayment_pisPaymentDataList_bulk_Success() {
        CmsPayment cmsPayment = cmsPsuPisMapper.mapToCmsPayment(PIS_PAYMENT_DATA_BULK_LIST);

        assertNotNull(cmsPayment);
        assertTrue(cmsPayment instanceof CmsBulkPayment);

        CmsBulkPayment bulkPayment = (CmsBulkPayment) cmsPayment;
        assertEquals(PAYMENT_ID, bulkPayment.getPaymentId());
        assertEquals(DEBTOR_CMS_ACCOUNT, bulkPayment.getDebtorAccount());
        assertFalse(bulkPayment.isBatchBookingPreferred());

        List<CmsSinglePayment> payments = bulkPayment.getPayments();
        assertTrue(CollectionUtils.isNotEmpty(payments));

        CmsSinglePayment singlePayment = payments.get(0);
        assertNotNull(singlePayment);
        assertEquals(PAYMENT_ID, singlePayment.getPaymentId());
        assertEquals(END_TO_END_IDENTIFICATION, singlePayment.getEndToEndIdentification());
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
        tppInfoEntity.setRedirectUri(REDIRECT_URI);
        tppInfoEntity.setNokRedirectUri(NOK_REDIRECT_URI);
        return tppInfoEntity;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        tppInfo.setTppRoles(TPP_ROLES);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
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
        return pisPaymentData;
    }

    private static PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }

    private static List<TppRole> buildTppRoles() {
        return Arrays.asList(TppRole.AISP, TppRole.ASPSP, TppRole.PIISP, TppRole.PISP);
    }

    private static TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri(REDIRECT_URI, NOK_REDIRECT_URI);
    }

    private static AccountReferenceEntity buildAccountReferenceEntity() {
        AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
        accountReferenceEntity.setIban(IBAN);
        return accountReferenceEntity;
    }

    private static CmsAccountReference buildCmsAccountReference() {
        CmsAccountReference cmsAccountReference = new CmsAccountReference();
        cmsAccountReference.setIban(IBAN);
        return cmsAccountReference;
    }

    private static PisAddress buildPisAddress() {
        PisAddress pisAddress = new PisAddress();
        pisAddress.setId(PSU_ADDRESS_ID);
        pisAddress.setCountry(COUNTRY);
        pisAddress.setCity(CITY);
        pisAddress.setStreet(STREET);
        pisAddress.setBuildingNumber(BUILDING_NUMBER);
        pisAddress.setPostalCode(POSTAL_CODE);
        return pisAddress;
    }

    private static CmsAddress buildCmsAddress() {
        CmsAddress cmsAddress = new CmsAddress();
        cmsAddress.setCountry(COUNTRY);
        cmsAddress.setCity(CITY);
        cmsAddress.setStreet(STREET);
        cmsAddress.setBuildingNumber(BUILDING_NUMBER);
        cmsAddress.setPostalCode(POSTAL_CODE);
        return cmsAddress;
    }
}
