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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
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
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CmsCommonPaymentMapperSupportImpl.class, Xs2aObjectMapper.class})
public class CmsCommonPaymentMapperSupportImplTest {

    private static final String PAYMENT_PRODUCT = "payments";

    @Autowired
    private CmsCommonPaymentMapperSupportImpl mapper;
    @Autowired
    private Xs2aObjectMapper xs2aObjectMapper;

    private JsonReader jsonReader = new JsonReader();
    private CmsCommonPayment cmsCommonPayment;

    @Before
    public void setUp() {
        cmsCommonPayment = createCmsCommonPayment();
    }

    @Test
    public void mapToCmsSinglePayment() throws JsonProcessingException {
        PaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/payment-initiation-resp.json", PaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsSinglePayment(cmsCommonPayment);

        CmsSinglePayment expected = getCmsSinglePayment(paymentInitiationJson);
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsSinglePayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsSinglePayment(cmsCommonPayment));
    }

    @Test
    public void mapToCmsBulkPayment() throws JsonProcessingException {
        BulkPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsBulkPayment(cmsCommonPayment);

        CmsBulkPayment expected = getCmsBulkPayment(paymentInitiationJson, cmsCommonPayment);
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsBulkPayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsBulkPayment(cmsCommonPayment));
    }

    @Test
    public void mapToCmsPeriodicPayment() throws JsonProcessingException {
        PeriodicPaymentInitiationJson paymentInitiationJson = jsonReader.getObjectFromFile("json/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        cmsCommonPayment.setPaymentData(xs2aObjectMapper.writeValueAsBytes(paymentInitiationJson));
        CmsPayment actual = mapper.mapToCmsPeriodicPayment(cmsCommonPayment);

        CmsPeriodicPayment expected = getCmsPeriodicPayment(paymentInitiationJson);
        assertEquals(expected, actual);
    }

    @Test
    public void mapToCmsPeriodicPayment_payDataIsNull() {
        cmsCommonPayment.setPaymentData(null);
        assertNull(mapper.mapToCmsPeriodicPayment(cmsCommonPayment));
    }

    private CmsPeriodicPayment getCmsPeriodicPayment(PeriodicPaymentInitiationJson paymentInitiationJson) {
        CmsPeriodicPayment payment = new CmsPeriodicPayment(PAYMENT_PRODUCT);
        payment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
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

        payment.setPayments(getBulkPayments(paymentInitiationJson.getPayments().get(0)));
        return payment;
    }

    private List<CmsSinglePayment> getBulkPayments(PaymentInitiationBulkElementJson paymentInitiationJson) {
        CmsSinglePayment singlePayment = new CmsSinglePayment(PAYMENT_PRODUCT);
        Amount instructedAmount = paymentInitiationJson.getInstructedAmount();
        singlePayment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
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
        return payment;
    }

    private CmsSinglePayment getCmsSinglePayment(PaymentInitiationJson paymentInitiationJson) {
        CmsSinglePayment payment = new CmsSinglePayment(PAYMENT_PRODUCT);
        payment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
        payment.setEndToEndIdentification(paymentInitiationJson.getEndToEndIdentification());
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
        payment.setPurposeCode(paymentInitiationJson.getPurposeCode().name());
        return payment;
    }

    @NotNull
    private CmsRemittance getRemittanceInformationStructured(RemittanceInformationStructured informationStructured) {
        CmsRemittance remittanceInformationStructured = new CmsRemittance();
        remittanceInformationStructured.setReference(informationStructured.getReference());
        remittanceInformationStructured.setReferenceType(informationStructured.getReferenceType());
        remittanceInformationStructured.setReferenceIssuer(informationStructured.getReferenceIssuer());
        return remittanceInformationStructured;
    }

    private CmsAccountReference getAccount(AccountReference accountReference) {
        CmsAccountReference cmsAccountReference = new CmsAccountReference();
        cmsAccountReference.setIban(accountReference.getIban());
        cmsAccountReference.setBban(accountReference.getBban());
        cmsAccountReference.setPan(accountReference.getPan());
        cmsAccountReference.setMaskedPan(accountReference.getMaskedPan());
        cmsAccountReference.setMsisdn(accountReference.getMsisdn());
        cmsAccountReference.setCurrency(Currency.getInstance(accountReference.getCurrency()));
        return cmsAccountReference;
    }

    private CmsAddress getCreditorAddress(Address address) {
        CmsAddress cmsAddress = new CmsAddress();
        cmsAddress.setCountry(address.getCountry());
        cmsAddress.setCity(address.getTownName());
        cmsAddress.setBuildingNumber(address.getBuildingNumber());
        cmsAddress.setStreet(address.getStreetName());
        cmsAddress.setPostalCode(address.getPostCode());
        return cmsAddress;
    }
}
