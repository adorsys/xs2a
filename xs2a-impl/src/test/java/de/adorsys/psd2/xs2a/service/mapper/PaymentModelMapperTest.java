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
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperPsd2;
import de.adorsys.psd2.xs2a.web.mapper.PaymentModelMapperXs2a;
import org.apache.commons.lang3.StringUtils;
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

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentModelMapperTest {

    private static final String PAYMENT_ID = "123456789";
    private static final String IBAN = "DE1234567890";
    private static final String CURRENCY = "EUR";
    private static final String DAY_OF_EXECUTION = "02";
    private static final boolean BATCH_BOOKING_PREFERRED = true;

    @InjectMocks
    PaymentModelMapperPsd2 paymentModelMapperPsd2;

    @InjectMocks
    PaymentModelMapperXs2a paymentModelMapperXs2a;

    @Mock
    ValueValidatorService validatorService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    MessageErrorMapper messageErrorMapper;

    @Spy
    AccountModelMapper accountModelMapper = new AccountModelMapper(new ObjectMapper());

    @Test
    public void mapToTransactionStatus12() {
        //Given
        TransactionStatus[] xs2aStatuses = TransactionStatus.values();
        de.adorsys.psd2.model.TransactionStatus[] statuses12 = de.adorsys.psd2.model.TransactionStatus.values();
        //When
        assertThat(xs2aStatuses.length).isEqualTo(statuses12.length);
        for (int i = 0; i < xs2aStatuses.length; i++) {
            testTransactionStatus12(xs2aStatuses[i], statuses12[i]);
        }
    }

    private void testTransactionStatus12(de.adorsys.psd2.xs2a.core.pis.TransactionStatus status, de.adorsys.psd2.model.TransactionStatus expected) {
        //When
        de.adorsys.psd2.model.TransactionStatus result = PaymentModelMapperPsd2.mapToTransactionStatus12(status);
        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void mapToXs2aPayment_Single_success() {
        when(objectMapper.convertValue(getSinglePayment(true, true, true, true, true, true, true), PaymentInitiationSctJson.class)).thenReturn(getSinglePayment12(true, true, true, true, true, true, true));
        when(objectMapper.convertValue(getAccountReference12Map(true, true), AccountReference.class)).thenReturn(getAccountReference(true, true));
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
        when(objectMapper.convertValue(getAccountReference12Map(true, true), AccountReference.class))
            .thenReturn(getAccountReference(true, true));
        //Given
        Object payment = getPeriodicPayment(true, true, true, true, true,
                                            true, true, true, true, true, true,
                                            true);
        //When
        PeriodicPayment result = (PeriodicPayment) paymentModelMapperXs2a.mapToXs2aPayment(payment, getRequestParameters(PaymentType.PERIODIC));
        //Then
        assertThat(result.getEndToEndIdentification()).isEqualTo(PAYMENT_ID);
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
        assertThat(result.getDayOfExecution().getValue()).isEqualTo(DAY_OF_EXECUTION);
    }

    @Test
    public void mapToXs2aPayment_Bulk_success() {
        when(objectMapper.convertValue(getBulkPayment(true, true, true,
                                                      true), BulkPaymentInitiationSctJson.class))
            .thenReturn(getBulkPayment(true, true, true, true));
        when(objectMapper.convertValue(getAccountReference12Map(true, true), AccountReference.class))
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
        assertThat(result.getPayments().get(0).getEndToEndIdentification()).isEqualTo(PAYMENT_ID);
    }

    //Static test data
    private LinkedHashMap<String, Object> getSinglePayment(boolean id, boolean acc, boolean amount, boolean agent, boolean creditorName, boolean credAddres, boolean remitance) {
        LinkedHashMap<String, Object> payment = new LinkedHashMap<>();
        payment.put("endToEndIdentification", id ? PAYMENT_ID : null);
        payment.put("debtorAccount", acc ? getAccountReference12Map(true, true) : null);
        payment.put("instructedAmount", amount ? getAmountMap12(true, true) : null);
        payment.put("creditorAccount", getAccountReference12Map(true, true));
        payment.put("creditorAgent", agent ? "Agent" : null);
        payment.put("creditorName", creditorName ? "CreditorName" : null);
        payment.put("creditorAddress", credAddres ? getAddress12Map(true, true, true, true, true) : null);
        payment.put("remittanceInformationUnstructured", remitance ? "some pmnt info" : null);
        return payment;
    }

    private PaymentInitiationSctJson getSinglePayment12(boolean id, boolean acc, boolean amount, boolean agent, boolean creditorName, boolean credAddres, boolean remitance) {
        PaymentInitiationSctJson payment = new PaymentInitiationSctJson();
        payment.setEndToEndIdentification(id ? PAYMENT_ID : null);
        payment.setDebtorAccount(acc ? getAccountReference12Map(true, true) : null);
        payment.setInstructedAmount(amount ? getAmount12(true, true) : null);
        payment.setCreditorAccount(getAccountReference12Map(true, true));
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
        payment.setRequestedExecutionDate(executionDate ? LocalDate.of(2017, 1, 1) : null);
        payment.setDebtorAccount(debtorAcc ? getAccountReference12Map(true, true) : null);

        PaymentInitiationSctBulkElementJson element = new PaymentInitiationSctBulkElementJson();
        element.setEndToEndIdentification(PAYMENT_ID);
        element.setInstructedAmount(getAmount12(true, true));
        element.setCreditorAccount(getAccountReference12Map(true, true));
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
        payment.setEndToEndIdentification(id ? PAYMENT_ID : null);
        payment.setDebtorAccount(acc ? getAccountReference12Map(true, true) : null);
        payment.setInstructedAmount(amount ? getAmount12(true, true) : null);
        payment.setCreditorAccount(getAccountReference12Map(true, true));
        payment.setCreditorAgent(agent ? "Agent" : null);
        payment.setCreditorName(creditorName ? "CreditorName" : null);
        payment.setCreditorAddress(credAddres ? getAddress12(true, true, true, true, true) : null);
        payment.setRemittanceInformationUnstructured(remitance ? "some pmnt info" : null);
        payment.setStartDate(startDate ? LocalDate.of(2017, 1, 1) : null);
        payment.setEndDate(endDate ? LocalDate.of(2017, 1, 2) : null);
        payment.setExecutionRule(execution ? ExecutionRule.FOLLOWING : null);
        payment.setFrequency(frequency ? FrequencyCode.DAILY : null);
        payment.setDayOfExecution(dayOfExecution ? DayOfExecution.fromValue(DAY_OF_EXECUTION) : null);
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

    private LinkedHashMap<String, Object> getAccountReference12Map(boolean iban, boolean currency) {
        LinkedHashMap<String, Object> ref = new LinkedHashMap<>();
        ref.put("iban", iban ? IBAN : null);
        ref.put("currency", currency ? CURRENCY : null);
        return ref;
    }

    private AccountReference getAccountReference(boolean iban, boolean currency) {
        AccountReference ref = new AccountReference();
        ref.setIban(iban ? IBAN : null);
        ref.setCurrency(currency ? Currency.getInstance(CURRENCY) : null);
        return ref;
    }

    private PaymentInitiationParameters getRequestParameters(PaymentType paymentType) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPaymentType(paymentType);
        requestParameters.setQwacCertificate("TEST CERTIFICATE");
        requestParameters.setPaymentProduct("sepa-credit-transfers");

        return requestParameters;
    }
}
