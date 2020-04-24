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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.*;
import de.adorsys.psd2.core.payment.model.*;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CmsCommonPaymentMapperSupportImpl.class, Xs2aObjectMapper.class})
class CmsCommonPaymentMapperSupportImplTest {

    private static final String PAYMENT_PRODUCT = "payments";
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";

    @Autowired
    private CmsCommonPaymentMapperSupportImpl mapper;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    private JsonReader jsonReader = new JsonReader();
    private CmsCommonPayment cmsCommonPayment;

    @BeforeEach
    void setUp() {
        cmsCommonPayment = createCmsCommonPayment();
    }

    @Test
    void mapToCmsSinglePayment() throws JsonProcessingException {
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/payment-initiation-resp.json", PaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsSinglePayment(cmsCommonPayment);

        CmsSinglePayment expected = getCmsSinglePayment(paymentInitiationJson, cmsCommonPayment);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsSinglePayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsSinglePayment(cmsCommonPayment));
    }

    @Test
    void mapToCmsBulkPayment() throws JsonProcessingException {
        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsBulkPayment(cmsCommonPayment);

        CmsBulkPayment expected = getCmsBulkPayment(paymentInitiationJson, cmsCommonPayment);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsBulkPayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsBulkPayment(cmsCommonPayment));
    }

    @Test
    void mapToCmsPeriodicPayment() throws JsonProcessingException {
        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsPeriodicPayment(cmsCommonPayment);

        CmsPeriodicPayment expected = getCmsPeriodicPayment(paymentInitiationJson);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsPeriodicPayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsPeriodicPayment(cmsCommonPayment));
    }

    @Test
    void mapToCmsPeriodicPayment_remittanceIsNull() throws JsonProcessingException {
        // Given
        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        paymentInitiationJson.setRemittanceInformationStructured(null);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));

        // When
        CmsPayment actual = mapper.mapToCmsPeriodicPayment(cmsCommonPayment);

        // Then
        CmsPeriodicPayment expected = getCmsPeriodicPayment(paymentInitiationJson);
        expected.setRemittanceInformationStructured(null);
        assertEquals(expected, actual);
    }

    @Test
    void mapToCmsSinglePayment_noRemittanceInformationStructuredArray() throws JsonProcessingException {
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/payment-initiation-resp.json", PaymentInitiationJson.class);
        paymentInitiationJson.setRemittanceInformationStructuredArray(null);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));

        CmsPayment actual = mapper.mapToCmsSinglePayment(cmsCommonPayment);

        assertTrue(actual instanceof CmsSinglePayment);
        CmsSinglePayment actualSinglePayment = (CmsSinglePayment) actual;
        assertEquals(Collections.emptyList(), actualSinglePayment.getRemittanceInformationStructuredArray());
    }

    private CmsPeriodicPayment getCmsPeriodicPayment(PeriodicPaymentInitiationJson paymentInitiationJson) {
        CmsPeriodicPayment payment = new CmsPeriodicPayment(PAYMENT_PRODUCT);
        payment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        payment.setInstructionIdentification(paymentInitiationJson.getInstructionIdentification());
        payment.setDebtorAccount(getAccount(paymentInitiationJson.getDebtorAccount()));
        Amount instructedAmount = paymentInitiationJson.getInstructedAmount();
        payment.setInstructedAmount(new CmsAmount(Currency.getInstance(instructedAmount.getCurrency()), BigDecimal.valueOf(Double.parseDouble(instructedAmount.getAmount()))));
        payment.setCreditorAccount(getAccount(paymentInitiationJson.getCreditorAccount()));
        payment.setCreditorAgent(paymentInitiationJson.getCreditorAgent());
        payment.setCreditorName(paymentInitiationJson.getCreditorName());
        payment.setCreditorAddress(getCreditorAddress(paymentInitiationJson.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(paymentInitiationJson.getRemittanceInformationUnstructured());
        payment.setDayOfExecution(PisDayOfExecution._14);
        payment.setStartDate(paymentInitiationJson.getStartDate());
        payment.setEndDate(paymentInitiationJson.getEndDate());
        payment.setExecutionRule(PisExecutionRule.FOLLOWING);
        payment.setFrequency(FrequencyCode.ANNUAL);
        payment.setUltimateDebtor(paymentInitiationJson.getUltimateDebtor());
        payment.setUltimateCreditor(paymentInitiationJson.getUltimateCreditor());
        payment.setPurposeCode(paymentInitiationJson.getPurposeCode().name());
        payment.setRemittanceInformationStructured(getRemittanceInformationStructured(paymentInitiationJson.getRemittanceInformationStructured()));
        payment.setRemittanceInformationStructuredArray(getRemittanceInformationStructuredArray(paymentInitiationJson.getRemittanceInformationStructuredArray()));

        payment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());
        payment.setPaymentId(cmsCommonPayment.getPaymentId());
        payment.setPsuIdDatas(cmsCommonPayment.getPsuIdDatas());
        payment.setTppInfo(cmsCommonPayment.getTppInfo());
        payment.setCreationTimestamp(cmsCommonPayment.getCreationTimestamp());
        payment.setStatusChangeTimestamp(cmsCommonPayment.getStatusChangeTimestamp());
        payment.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);

        return payment;
    }

    private CmsBulkPayment getCmsBulkPayment(BulkPaymentInitiationJson paymentInitiationJson, CmsCommonPayment cmsCommonPayment) {
        CmsBulkPayment payment = new CmsBulkPayment();
        payment.setPaymentProduct(cmsCommonPayment.getPaymentProduct());
        payment.setPaymentId(cmsCommonPayment.getPaymentId());
        payment.setTppInfo(cmsCommonPayment.getTppInfo());
        payment.setPsuIdDatas(cmsCommonPayment.getPsuIdDatas());
        payment.setCreationTimestamp(cmsCommonPayment.getCreationTimestamp());
        payment.setStatusChangeTimestamp(cmsCommonPayment.getStatusChangeTimestamp());

        payment.setBatchBookingPreferred(paymentInitiationJson.getBatchBookingPreferred());
        payment.setDebtorAccount((getAccount(paymentInitiationJson.getDebtorAccount())));
        payment.setBatchBookingPreferred(paymentInitiationJson.getBatchBookingPreferred());
        payment.setRequestedExecutionDate(paymentInitiationJson.getRequestedExecutionDate());
        payment.setPaymentStatus(cmsCommonPayment.getTransactionStatus());

        payment.setPayments(getBulkPayments(paymentInitiationJson.getPayments().get(0), cmsCommonPayment));
        payment.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);
        return payment;
    }

    private List<CmsSinglePayment> getBulkPayments(PaymentInitiationBulkElementJson paymentInitiationJson, CmsPayment parent) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        Amount instructedAmount = paymentInitiationJson.getInstructedAmount();
        singlePayment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        singlePayment.setInstructionIdentification(paymentInitiationJson.getInstructionIdentification());
        singlePayment.setInstructedAmount(new CmsAmount(Currency.getInstance("EUR"), new BigDecimal(instructedAmount.getAmount())));
        singlePayment.setCreditorAccount(getAccount(paymentInitiationJson.getCreditorAccount()));
        singlePayment.setCreditorAgent(paymentInitiationJson.getCreditorAgent());
        singlePayment.setCreditorName(paymentInitiationJson.getCreditorName());
        singlePayment.setCreditorAddress(getCreditorAddress(paymentInitiationJson.getCreditorAddress()));
        singlePayment.setRemittanceInformationUnstructured(paymentInitiationJson.getRemittanceInformationUnstructured());
        singlePayment.setPaymentStatus(TransactionStatus.RCVD);
        singlePayment.setPurposeCode(paymentInitiationJson.getPurposeCode().name());
        singlePayment.setUltimateDebtor(paymentInitiationJson.getUltimateDebtor());
        singlePayment.setUltimateCreditor(paymentInitiationJson.getUltimateCreditor());
        singlePayment.setRemittanceInformationStructured(getRemittanceInformationStructured(paymentInitiationJson.getRemittanceInformationStructured()));
        singlePayment.setRemittanceInformationStructuredArray(getRemittanceInformationStructuredArray(paymentInitiationJson.getRemittanceInformationStructuredArray()));
        singlePayment.setPaymentId(parent.getPaymentId());
        singlePayment.setPsuIdDatas(parent.getPsuIdDatas());
        singlePayment.setTppInfo(parent.getTppInfo());
        singlePayment.setStatusChangeTimestamp(parent.getStatusChangeTimestamp());
        singlePayment.setCreationTimestamp(parent.getCreationTimestamp());
        return Collections.singletonList(singlePayment);
    }

    private CmsCommonPayment createCmsCommonPayment() {
        CmsCommonPayment payment = new CmsCommonPayment(PAYMENT_PRODUCT);
        payment.setTransactionStatus(TransactionStatus.RCVD);
        payment.setPaymentId("2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI");
        payment.setPsuIdDatas(Collections.singletonList(new PsuIdData("PSU ID", "PSU ID TYPE", "PSU CORPORATE ID", "PSU CORPORATE ID TYPE", "PSU IP ADDRESS")));
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("12ert8984375hsdfnms");
        tppInfo.setTppName("Tpp company");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authority id");
        tppInfo.setAuthorityName("authority name");
        tppInfo.setCountry("Germany");
        tppInfo.setOrganisation("Organisation");
        tppInfo.setOrganisationUnit("Organisation unit");
        tppInfo.setCity("Nuremberg");
        tppInfo.setState("Bayern");
        payment.setTppInfo(tppInfo);
        payment.setCreationTimestamp(OffsetDateTime.now());
        payment.setStatusChangeTimestamp(OffsetDateTime.now());
        payment.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);
        return payment;
    }

    private CmsSinglePayment getCmsSinglePayment(PaymentInitiationJson paymentInitiationJson, CmsPayment parent) {
        CmsSinglePayment payment = new CmsSinglePayment(PAYMENT_PRODUCT);
        payment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        payment.setInstructionIdentification(paymentInitiationJson.getInstructionIdentification());
        payment.setDebtorAccount(getAccount(paymentInitiationJson.getDebtorAccount()));
        Amount instructedAmount = paymentInitiationJson.getInstructedAmount();
        payment.setInstructedAmount(new CmsAmount(Currency.getInstance("EUR"), new BigDecimal(instructedAmount.getAmount())));
        payment.setCreditorAccount(getAccount(paymentInitiationJson.getCreditorAccount()));
        payment.setCreditorAgent(paymentInitiationJson.getCreditorAgent());
        payment.setCreditorName(paymentInitiationJson.getCreditorName());
        payment.setCreditorAddress(getCreditorAddress(paymentInitiationJson.getCreditorAddress()));
        payment.setRemittanceInformationUnstructured(paymentInitiationJson.getRemittanceInformationUnstructured());
        payment.setRequestedExecutionDate(paymentInitiationJson.getRequestedExecutionDate());
        payment.setPaymentStatus(TransactionStatus.RCVD);
        payment.setUltimateDebtor(paymentInitiationJson.getUltimateDebtor());
        payment.setUltimateCreditor(paymentInitiationJson.getUltimateCreditor());
        payment.setRemittanceInformationStructured(getRemittanceInformationStructured(paymentInitiationJson.getRemittanceInformationStructured()));
        payment.setRemittanceInformationStructuredArray(getRemittanceInformationStructuredArray(paymentInitiationJson.getRemittanceInformationStructuredArray()));
        payment.setPurposeCode(paymentInitiationJson.getPurposeCode().name());
        payment.setTppBrandLoggingInformation(TPP_BRAND_LOGGING_INFORMATION);

        payment.setPaymentId(parent.getPaymentId());
        payment.setPsuIdDatas(parent.getPsuIdDatas());
        payment.setTppInfo(parent.getTppInfo());
        payment.setCreationTimestamp(parent.getCreationTimestamp());
        payment.setStatusChangeTimestamp(parent.getStatusChangeTimestamp());
        return payment;
    }

    private List<CmsRemittance> getRemittanceInformationStructuredArray(RemittanceInformationStructuredArray informationStructuredArray) {
        if (informationStructuredArray == null) {
            return Collections.emptyList();
        }

        return informationStructuredArray.stream()
                   .map(this::getRemittanceInformationStructured)
                   .collect(Collectors.toList());
    }

    private CmsRemittance getRemittanceInformationStructured(RemittanceInformationStructured informationStructured) {
        if (informationStructured == null) {
            return null;
        }

        CmsRemittance remittanceInformationStructured = new CmsRemittance();
        remittanceInformationStructured.setReference(informationStructured.getReference());
        remittanceInformationStructured.setReferenceType(informationStructured.getReferenceType());
        remittanceInformationStructured.setReferenceIssuer(informationStructured.getReferenceIssuer());
        return remittanceInformationStructured;
    }

    private de.adorsys.psd2.xs2a.core.profile.AccountReference getAccount(AccountReference accountReference) {
        de.adorsys.psd2.xs2a.core.profile.AccountReference xs2aAccountReference = new de.adorsys.psd2.xs2a.core.profile.AccountReference();
        xs2aAccountReference.setIban(accountReference.getIban());
        xs2aAccountReference.setBban(accountReference.getBban());
        xs2aAccountReference.setPan(accountReference.getPan());
        xs2aAccountReference.setMaskedPan(accountReference.getMaskedPan());
        xs2aAccountReference.setMsisdn(accountReference.getMsisdn());
        xs2aAccountReference.setCurrency(Currency.getInstance(accountReference.getCurrency()));
        return xs2aAccountReference;
    }

    private CmsAddress getCreditorAddress(Address address) {
        CmsAddress cmsAddress = new CmsAddress();
        cmsAddress.setCountry(address.getCountry());
        cmsAddress.setTownName(address.getTownName());
        cmsAddress.setBuildingNumber(address.getBuildingNumber());
        cmsAddress.setStreetName(address.getStreetName());
        cmsAddress.setPostCode(address.getPostCode());
        return cmsAddress;
    }
}
