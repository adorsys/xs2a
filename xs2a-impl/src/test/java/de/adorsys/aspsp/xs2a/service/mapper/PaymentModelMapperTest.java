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

package de.adorsys.aspsp.xs2a.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.Xs2aAmount;
import de.adorsys.aspsp.xs2a.domain.Xs2aTransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.pis.*;
import de.adorsys.aspsp.xs2a.service.validator.ValueValidatorService;
import de.adorsys.psd2.model.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Currency;
import java.util.LinkedHashMap;

import static de.adorsys.aspsp.xs2a.domain.pis.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentModelMapperTest {

    private static final String PAYMENT_ID = "123456789";
    private static final String AMOUNT = "1000";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String PSU_MSG = "Payment is success";
    private static final String PATH = "https:\\test.com";
    private static final String MSG_TEXT = "Some error message";
    private static final String IBAN = "DE1234567890";
    private static final String CURRENCY = "EUR";

    @InjectMocks
    PaymentModelMapper paymentModelMapper;

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
        Xs2aTransactionStatus[] xs2aStatuses = Xs2aTransactionStatus.values();
        de.adorsys.psd2.model.TransactionStatus[] statuses12 = de.adorsys.psd2.model.TransactionStatus.values();
        //When
        assertThat(xs2aStatuses.length).isEqualTo(statuses12.length);
        for (int i = 0; i < xs2aStatuses.length; i++) {
            testTransactionStatus12(xs2aStatuses[i], statuses12[i]);
        }
    }

    private void testTransactionStatus12(Xs2aTransactionStatus status, de.adorsys.psd2.model.TransactionStatus expected) {
        //When
        de.adorsys.psd2.model.TransactionStatus result = PaymentModelMapper.mapToTransactionStatus12(status);
        //Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void mapToPaymentInitiationResponse12() {
        //Given
        PaymentInitialisationResponse givenResponse = getXs2aPaymentResponse();
        PaymentInitationRequestResponse201 expectedResponse = getPaymentResponse12();
        //When
        PaymentInitationRequestResponse201 result = (PaymentInitationRequestResponse201) paymentModelMapper.mapToPaymentInitiationResponse12(givenResponse, getRequestParameters(SINGLE));
        //Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    public void mapToXs2aPayment_Single_success() {
        when(objectMapper.convertValue(getSinglePayment(true, true, true, true, true, true, true), PaymentInitiationSctJson.class)).thenReturn(getSinglePayment12(true, true, true, true, true, true, true));
        when(objectMapper.convertValue(getAccountReference12Map(true, true), Xs2aAccountReference.class)).thenReturn(getAccountReference(true, true));
        //Given
        Object payment = getSinglePayment(true, true, true, true, true, true, true);
        //When
        SinglePayment result = (SinglePayment) paymentModelMapper.mapToXs2aPayment(payment, getRequestParameters(SINGLE));
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
        assertThat(result.getRequestedExecutionDate()).isNotNull();
        assertThat(result.getRequestedExecutionTime()).isNotNull();
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

    private de.adorsys.psd2.model.Amount getAmount12(boolean currency, boolean toPay) {
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

    private Xs2aAccountReference getAccountReference(boolean iban, boolean currency) {
        Xs2aAccountReference ref = new Xs2aAccountReference();
        ref.setIban(iban ? IBAN : null);
        ref.setCurrency(currency ? Currency.getInstance(CURRENCY) : null);
        return ref;
    }

    private PaymentInitationRequestResponse201 getPaymentResponse12() {
        PaymentInitationRequestResponse201 response = new PaymentInitationRequestResponse201();

        response.setTransactionStatus(de.adorsys.psd2.model.TransactionStatus.ACCP);
        response.setPaymentId(PAYMENT_ID);
        de.adorsys.psd2.model.Amount amount = new de.adorsys.psd2.model.Amount();
        amount.setAmount(AMOUNT);
        amount.setCurrency(EUR.getCurrencyCode());
        response.setTransactionFees(amount);
        response.setTransactionFeeIndicator(true);
        response.setScaMethods(null);
        response.setChosenScaMethod(null);
        response.setChallengeData(null);
        response.setLinks(null);
        response.setPsuMessage(PSU_MSG);

        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setPath(PATH);
        tppMessage.setText(MSG_TEXT);
        tppMessage.setCode(null);
        TppMessages messages = new TppMessages();
        messages.add(tppMessage);

        response.setTppMessages(null); //TODO fix this along with creating TppMessage mapper
        return response;
    }

    private PaymentInitialisationResponse getXs2aPaymentResponse() {
        PaymentInitialisationResponse response = new PaymentInitialisationResponse();
        response.setTransactionStatus(Xs2aTransactionStatus.ACCP);
        response.setPaymentId(PAYMENT_ID);

        response.setTransactionFees(getXs2aAmount());
        response.setTransactionFeeIndicator(true);
        response.setScaMethods(null);
        response.setPsuMessage(PSU_MSG);
        response.setTppMessages(null); //TODO fix this along with creating TppMessage mapper
        response.setLinks(null);
        response.setTppRedirectPreferred(false);
        return response;
    }

    private Xs2aAmount getXs2aAmount() {
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount(AMOUNT);
        amount.setCurrency(EUR);
        return amount;
    }

    private PaymentRequestParameters getRequestParameters(PaymentType paymentType){
        PaymentRequestParameters requestParameters = new PaymentRequestParameters();
        requestParameters.setPaymentType(paymentType);
        requestParameters.setQwacCertificate("TEST CERTIFICATE");
        requestParameters.setPaymentProduct(PaymentProduct.SCT);

        return requestParameters;
    }
}
