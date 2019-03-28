/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentModelMapperTest {

    private static final boolean BATCH_BOOKING_PREFERRED = true;
    private static final String END_TO_END_IDENTIFICATION = "123456789";
    private static final String IBAN = "DE1234567890";
    private static final String CURRENCY = "EUR";
    private static final String STANDARD_PAYMENT_TYPE = "sepa-credit-transfers";
    private static final String NON_STANDARD_PAYMENT_TYPE = "pain.001-sepa-credit-transfers";
    private static final String NON_STANDARD_PAYMENT_DATA_STRING = "Test payment data";
    private static final String CREDITOR_AGENT = "TestAgent";
    private static final String CREDITOR_NAME = "TestAgentName";
    private static final String REMITTANCE_INFORMATION_UNSTRUCTED = "Test remmitanse info";
    private static final LocalDate START_DATE = LocalDate.of(2020, 1, 2);
    private static final LocalDate END_DATE = LocalDate.of(2020, 6, 2);
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.of(2020, 2, 15);
    private static final DayOfExecution PSD2_DAY_OF_EXECUTION = DayOfExecution._2;
    private static final ExecutionRule PSD2_EXECUTION_RULE = ExecutionRule.FOLLOWING;
    private static final FrequencyCode PSD2_FREQUENCY_CODE = FrequencyCode.DAILY;
    private static final PisDayOfExecution XS2A_DAY_OF_EXECUTION = PisDayOfExecution._2;
    private static final PisExecutionRule XS2A_EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final Xs2aFrequencyCode XS2A_FREQUENCY_CODE = Xs2aFrequencyCode.DAILY;

    @InjectMocks
    PaymentModelMapperPsd2 paymentModelMapperPsd2;

    @InjectMocks
    PaymentModelMapperXs2a paymentModelMapperXs2a;

    @Mock
    ValueValidatorService validatorService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private AmountModelMapper amountModelMapper;

    @Mock
    private StandardPaymentProductsResolver standardPaymentProductsResolver;

    @Spy
    AccountModelMapper accountModelMapper = new AccountModelMapper(amountModelMapper, new HrefLinkMapper(new ObjectMapper()));

    @Before
    public void setUp() {
        when(amountModelMapper.mapToXs2aAmount(getAmount12(true, true))).thenReturn(buildXs2aAmount());
        when(amountModelMapper.mapToAmount(buildXs2aAmount())).thenReturn(getAmount12(true, true));
        when(standardPaymentProductsResolver.isRawPaymentProduct(STANDARD_PAYMENT_TYPE)).thenReturn(false);
        when(standardPaymentProductsResolver.isRawPaymentProduct(NON_STANDARD_PAYMENT_TYPE)).thenReturn(true);
    }

    @Test
    public void mapToXs2aPayment_Single_success() {
        when(objectMapper.convertValue(getSinglePayment(true, true, true, true, true, true, true), PaymentInitiationSctJson.class)).thenReturn(getSinglePayment12(true, true, true, true, true, true, true));
        when(objectMapper.convertValue(getPsd2AccountReference(true, true), AccountReference.class)).thenReturn(getAccountReference(true, true));
        //Given
        Object payment = getSinglePayment(true, true, true, true, true, true, true);
        //When
        SinglePayment result = (SinglePayment) paymentModelMapperXs2a.mapToXs2aPayment(payment, getRequestParameters(SINGLE));
        //Then
        assertThat(result.getEndToEndIdentification()).isEqualTo(((LinkedHashMap) payment).get("endToEndIdentification"));
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getDebtorAccount().getIban()).isEqualTo(IBAN);
        assertThat(result.getDebtorAccount().getCurrency()).isEqualTo(Currency.getInstance(CURRENCY));
        assertThat(result.getUltimateDebtor()).isNotNull();
        assertThat(result.getInstructedAmount()).isNotNull();
        assertThat(result.getCreditorAccount()).isNotNull();
        assertThat(result.getCreditorAgent()).isNotNull();
        assertThat(StringUtils.isNotBlank(result.getCreditorName())).isTrue();
        assertThat(result.getCreditorAddress()).isNotNull();
        assertThat(result.getPurposeCode()).isNotNull();
        assertThat(result.getRemittanceInformationStructured()).isNotNull();
        assertThat(result.getUltimateCreditor()).isNotBlank();
        assertThat(result.getRemittanceInformationUnstructured()).isNotBlank();
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getRequestedExecutionDate()).isNull();
        assertThat(result.getRequestedExecutionTime()).isNull();
    }

    @Test
    public void mapToXs2aPayment_Periodic_success() {
        when(objectMapper.convertValue(getPeriodicPayment(true, true, true, true,
                                                          true, true, true, true, true, true,
                                                          true, true), PeriodicPaymentInitiationSctJson.class))
            .thenReturn(getPeriodicPayment(true, true, true, true, true,
                                           true, true, true, true, true, true,
                                           true));
        when(objectMapper.convertValue(getPsd2AccountReference(true, true), AccountReference.class))
            .thenReturn(getAccountReference(true, true));
        //Given
        Object payment = getPeriodicPayment(true, true, true, true, true,
                                            true, true, true, true, true, true,
                                            true);
        //When
        PeriodicPayment result = (PeriodicPayment) paymentModelMapperXs2a.mapToXs2aPayment(payment, getRequestParameters(PaymentType.PERIODIC));
        //Then
        assertThat(result.getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getDebtorAccount().getIban()).isEqualTo(IBAN);
        assertThat(result.getDebtorAccount().getCurrency()).isEqualTo(Currency.getInstance(CURRENCY));
        assertThat(result.getInstructedAmount()).isNotNull();
        assertThat(result.getCreditorAccount()).isNotNull();
        assertThat(result.getCreditorAgent()).isNotNull();
        assertThat(StringUtils.isNotBlank(result.getCreditorName())).isTrue();
        assertThat(result.getCreditorAddress()).isNotNull();
        assertThat(result.getRemittanceInformationUnstructured()).isNotBlank();
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getStartDate()).isNotNull();
        assertThat(result.getExecutionRule().getValue()).isNotBlank();
        assertThat(result.getEndDate()).isNotNull();
        assertThat(result.getFrequency()).isNotNull();
        assertThat(result.getDayOfExecution().name()).isEqualTo(PSD2_DAY_OF_EXECUTION.name());
    }

    @Test
    public void mapToXs2aPayment_Bulk_success() {
        when(objectMapper.convertValue(getBulkPayment(true, true, true,
                                                      true), BulkPaymentInitiationSctJson.class))
            .thenReturn(getBulkPayment(true, true, true, true));
        when(objectMapper.convertValue(getPsd2AccountReference(true, true), AccountReference.class))
            .thenReturn(getAccountReference(true, true));
        //Given
        Object payment = getBulkPayment(true, true, true, true);
        //When
        BulkPayment result = (BulkPayment) paymentModelMapperXs2a.mapToXs2aPayment(payment, getRequestParameters(PaymentType.BULK));
        //Then
        assertThat(result.getBatchBookingPreferred()).isEqualTo(BATCH_BOOKING_PREFERRED);
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getRequestedExecutionDate()).isNotNull();
        assertThat(result.getPayments()).isNotEmpty();
        assertThat(result.getPayments().get(0).getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
    }

    @Test
    public void mapToGetPaymentResponse12_Single_success() {
        //When
        PaymentInitiationTarget2WithStatusResponse result = (PaymentInitiationTarget2WithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse12(buildSinglePayment(TransactionStatus.RCVD), SINGLE, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(result.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(result.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(result.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);
    }

    @Test
    public void mapToGetPaymentResponse12_Periodic_success() {
        //When
        PeriodicPaymentInitiationTarget2WithStatusResponse result = (PeriodicPaymentInitiationTarget2WithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse12(buildPeriodicPayment(TransactionStatus.RCVD), PERIODIC, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getEndToEndIdentification()).isEqualTo(END_TO_END_IDENTIFICATION);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(result.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(result.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(result.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
        assertThat(result.getStartDate()).isEqualTo(START_DATE);
        assertThat(result.getEndDate()).isEqualTo(END_DATE);
        assertThat(result.getExecutionRule()).isEqualTo(PSD2_EXECUTION_RULE);
        assertThat(result.getFrequency()).isEqualTo(PSD2_FREQUENCY_CODE);
        assertThat(result.getDayOfExecution()).isEqualTo(PSD2_DAY_OF_EXECUTION);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);
    }

    @Test
    public void mapToGetPaymentResponse12_Bulk_success() {
        //When
        BulkPaymentInitiationTarget2WithStatusResponse result = (BulkPaymentInitiationTarget2WithStatusResponse) paymentModelMapperPsd2.mapToGetPaymentResponse12(buildBulkPayment(TransactionStatus.RCVD), BULK, STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getPayments()).isNotNull();
        assertThat(result.getPayments()).isNotEmpty();
        assertThat(result.getBatchBookingPreferred()).isEqualTo(BATCH_BOOKING_PREFERRED);
        assertThat(result.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(result.getRequestedExecutionDate()).isEqualTo(REQUESTED_EXECUTION_DATE);
        assertThat(result.getTransactionStatus()).isEqualTo(de.adorsys.psd2.model.TransactionStatus.RCVD);

        PaymentInitiationTarget2Json bulkPaymentPart = result.getPayments().get(0);
        assertThat(bulkPaymentPart.getDebtorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(bulkPaymentPart.getInstructedAmount()).isEqualTo(getAmount12(true, true));
        assertThat(bulkPaymentPart.getCreditorAccount()).isEqualTo(getPsd2AccountReference(true, true));
        assertThat(bulkPaymentPart.getCreditorAgent()).isEqualTo(CREDITOR_AGENT);
        assertThat(bulkPaymentPart.getCreditorName()).isEqualTo(CREDITOR_NAME);
        assertThat(bulkPaymentPart.getCreditorAddress()).isEqualTo(getAddress12(true, true, true, true, true));
        assertThat(bulkPaymentPart.getRemittanceInformationUnstructured()).isEqualTo(REMITTANCE_INFORMATION_UNSTRUCTED);
    }

    @Test
    public void mapToGetPaymentResponse12_NonStandardFormat_success() {
        //When
        String result = (String) paymentModelMapperPsd2.mapToGetPaymentResponse12(buildNonStandardPayment(), any(), NON_STANDARD_PAYMENT_TYPE);

        //Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(NON_STANDARD_PAYMENT_DATA_STRING);
    }

    //Static test data
    private LinkedHashMap<String, Object> getSinglePayment(boolean id, boolean acc, boolean amount, boolean agent, boolean creditorName, boolean credAddres, boolean remitance) {
        LinkedHashMap<String, Object> payment = new LinkedHashMap<>();
        payment.put("endToEndIdentification", id ? END_TO_END_IDENTIFICATION : null);
        payment.put("debtorAccount", acc ? getPsd2AccountReference(true, true) : null);
        payment.put("instructedAmount", amount ? getAmountMap12(true, true) : null);
        payment.put("creditorAccount", getPsd2AccountReference(true, true));
        payment.put("creditorAgent", agent ? "Agent" : null);
        payment.put("creditorName", creditorName ? "CreditorName" : null);
        payment.put("creditorAddress", credAddres ? getAddress12Map(true, true, true, true, true) : null);
        payment.put("remittanceInformationUnstructured", remitance ? "some pmnt info" : null);
        return payment;
    }

    private PaymentInitiationSctJson getSinglePayment12(boolean id, boolean acc, boolean amount, boolean agent, boolean creditorName, boolean credAddres, boolean remitance) {
        PaymentInitiationSctJson payment = new PaymentInitiationSctJson();
        payment.setEndToEndIdentification(id ? END_TO_END_IDENTIFICATION : null);
        payment.setDebtorAccount(acc ? getPsd2AccountReference(true, true) : null);
        payment.setInstructedAmount(amount ? getAmount12(true, true) : null);
        payment.setCreditorAccount(getPsd2AccountReference(true, true));
        payment.setCreditorAgent(agent ? "Agent" : null);
        payment.setCreditorName(creditorName ? "CreditorName" : null);
        payment.setCreditorAddress(credAddres ? getAddress12(true, true, true, true, true) : null);
        payment.setRemittanceInformationUnstructured(remitance ? "some pmnt info" : null);
        return payment;
    }

    private BulkPaymentInitiationSctJson getBulkPayment(boolean batchBooking, boolean executionDate,
                                                        boolean debtorAcc, boolean payments) {
        BulkPaymentInitiationSctJson payment = new BulkPaymentInitiationSctJson();
        payment.setBatchBookingPreferred(batchBooking ? BATCH_BOOKING_PREFERRED : null);
        payment.setRequestedExecutionDate(executionDate ? REQUESTED_EXECUTION_DATE : null);
        payment.setDebtorAccount(debtorAcc ? getPsd2AccountReference(true, true) : null);

        PaymentInitiationSctBulkElementJson element = new PaymentInitiationSctBulkElementJson();
        element.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        element.setInstructedAmount(getAmount12(true, true));
        element.setCreditorAccount(getPsd2AccountReference(true, true));
        element.setCreditorAgent("Agent");
        element.setCreditorName("CreditorName");
        element.setCreditorAddress(getAddress12(true, true, true, true, true));
        element.setRemittanceInformationUnstructured("some info");
        List<PaymentInitiationSctBulkElementJson> elements = Collections.singletonList(element);

        payment.setPayments(payments ? elements : null);
        return payment;
    }

    private PeriodicPaymentInitiationSctJson getPeriodicPayment(boolean id, boolean acc, boolean amount, boolean agent,
                                                                boolean creditorName, boolean credAddres,
                                                                boolean remitance, boolean startDate, boolean endDate,
                                                                boolean execution, boolean frequency,
                                                                boolean dayOfExecution) {
        PeriodicPaymentInitiationSctJson payment = new PeriodicPaymentInitiationSctJson();
        payment.setEndToEndIdentification(id ? END_TO_END_IDENTIFICATION : null);
        payment.setDebtorAccount(acc ? getPsd2AccountReference(true, true) : null);
        payment.setInstructedAmount(amount ? getAmount12(true, true) : null);
        payment.setCreditorAccount(getPsd2AccountReference(true, true));
        payment.setCreditorAgent(agent ? "Agent" : null);
        payment.setCreditorName(creditorName ? "CreditorName" : null);
        payment.setCreditorAddress(credAddres ? getAddress12(true, true, true, true, true) : null);
        payment.setRemittanceInformationUnstructured(remitance ? "some pmnt info" : null);
        payment.setStartDate(startDate ? START_DATE : null);
        payment.setEndDate(endDate ? END_DATE : null);
        payment.setExecutionRule(execution ? ExecutionRule.FOLLOWING : null);
        payment.setFrequency(frequency ? FrequencyCode.DAILY : null);
        payment.setDayOfExecution(dayOfExecution ? PSD2_DAY_OF_EXECUTION : null);
        return payment;
    }

    private LinkedHashMap<String, Object> getAddress12Map(boolean code, boolean str, boolean bld, boolean city, boolean country) {
        LinkedHashMap<String, Object> address = new LinkedHashMap<>();
        address.put("postalCode", code ? "PostalCode" : null);
        address.put("city", city ? "Kiev" : null);
        address.put("buildingNumber", bld ? "8" : null);
        address.put("street", str ? "Esplanadnaya" : null);
        address.put("country", country ? "Ukraine" : null);
        return address;
    }

    private Address getAddress12(boolean code, boolean str, boolean bld, boolean city, boolean country) {
        Address address = new Address();
        address.setPostalCode(code ? "PostalCode" : null);
        address.setCity(city ? "Kiev" : null);
        address.setBuildingNumber(bld ? "8" : null);
        address.setStreet(str ? "Esplanadnaya" : null);
        address.setCountry(country ? "Ukraine" : null);
        return address;
    }

    private LinkedHashMap<String, Object> getAmountMap12(boolean currency, boolean toPay) {
        LinkedHashMap<String, Object> instructedAmount = new LinkedHashMap<>();
        instructedAmount.put("currency", currency ? "EUR" : null);
        instructedAmount.put("amount", toPay ? "123456" : null);
        return instructedAmount;
    }

    private Amount getAmount12(boolean currency, boolean toPay) {
        de.adorsys.psd2.model.Amount instructedAmount = new de.adorsys.psd2.model.Amount();
        instructedAmount.setCurrency(currency ? "EUR" : null);
        instructedAmount.setAmount(toPay ? "123456" : null);
        return instructedAmount;
    }

    private de.adorsys.psd2.model.AccountReference getPsd2AccountReference(boolean iban, boolean currency) {
        de.adorsys.psd2.model.AccountReference accountReference = new de.adorsys.psd2.model.AccountReference();
        accountReference.setIban(iban ? IBAN : null);
        accountReference.setCurrency(currency ? CURRENCY : null);
        return accountReference;
    }

    private AccountReference getAccountReference(boolean iban, boolean currency) {
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(iban ? IBAN : null);
        accountReference.setCurrency(currency ? Currency.getInstance(CURRENCY) : null);
        return accountReference;
    }

    private PaymentInitiationParameters getRequestParameters(PaymentType paymentType) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(paymentType);
        requestParameters.setQwacCertificate("TEST CERTIFICATE");
        requestParameters.setPaymentProduct("sepa-credit-transfers");

        return requestParameters;
    }

    private Xs2aAmount buildXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setCurrency(Currency.getInstance("EUR"));
        amount.setAmount("123456");
        return amount;
    }

    private PisPaymentInfo buildNonStandardPayment() {
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentData(NON_STANDARD_PAYMENT_DATA_STRING.getBytes());
        return paymentInfo;
    }

    private Xs2aAddress buildXs2aAddress() {
        Xs2aAddress address = new Xs2aAddress();
        address.setCountry(new Xs2aCountryCode("Ukraine"));
        address.setCity("Kiev");
        address.setPostalCode("PostalCode");
        address.setStreet("Esplanadnaya");
        address.setBuildingNumber("8");
        return address;
    }

    private SinglePayment buildSinglePayment(TransactionStatus status) {
        SinglePayment payment = new SinglePayment();
        payment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setInstructedAmount(buildXs2aAmount());
        payment.setCreditorAccount(getAccountReference(true, true));
        payment.setCreditorAgent(CREDITOR_AGENT);
        payment.setCreditorName(CREDITOR_NAME);
        payment.setCreditorAddress(buildXs2aAddress());
        payment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTED);
        payment.setTransactionStatus(status);
        return payment;
    }

    private PeriodicPayment buildPeriodicPayment(TransactionStatus status) {
        PeriodicPayment payment = new PeriodicPayment();
        payment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setInstructedAmount(buildXs2aAmount());
        payment.setCreditorAccount(getAccountReference(true, true));
        payment.setCreditorAgent(CREDITOR_AGENT);
        payment.setCreditorName(CREDITOR_NAME);
        payment.setCreditorAddress(buildXs2aAddress());
        payment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTED);
        payment.setStartDate(START_DATE);
        payment.setEndDate(END_DATE);
        payment.setExecutionRule(XS2A_EXECUTION_RULE);
        payment.setFrequency(XS2A_FREQUENCY_CODE);
        payment.setDayOfExecution(XS2A_DAY_OF_EXECUTION);
        payment.setTransactionStatus(status);
        return payment;
    }

    private BulkPayment buildBulkPayment(TransactionStatus status) {
        BulkPayment payment = new BulkPayment();
        payment.setBatchBookingPreferred(BATCH_BOOKING_PREFERRED);
        payment.setDebtorAccount(getAccountReference(true, true));
        payment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        payment.setPayments(Collections.singletonList(buildSinglePayment(status)));
        payment.setTransactionStatus(status);
        return payment;
    }
}
