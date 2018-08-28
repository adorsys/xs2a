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

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentProduct;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Currency;
import java.util.LinkedHashMap;

import static de.adorsys.aspsp.xs2a.service.mapper.PaymentModelMapper.mapToXs2aPayment;
import static org.assertj.core.api.Assertions.assertThat;

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

    private void testTransactionStatus12(TransactionStatus status, de.adorsys.psd2.model.TransactionStatus expected) {
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
        PaymentInitationRequestResponse201 result = PaymentModelMapper.mapToPaymentInitiationResponse12(givenResponse, PaymentType.SINGLE, PaymentProduct.SCT);
        //Then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    public void mapToXs2aPayment_Single_success() {
        //Given:
        Object payment = getSinglePayment(true, true, true, true, true, true, true);
        SinglePayment result = mapToXs2aPayment(payment, PaymentType.SINGLE, PaymentProduct.SCT);
        System.out.println(result.toString());

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

    @Test
    public void mapToXs2aPayment_Single_null() {
        //Given:
        Object payment = getSinglePayment(false, false, false, false, false, false, false);
        SinglePayment result = mapToXs2aPayment(payment, PaymentType.SINGLE, PaymentProduct.SCT);
        System.out.println(result.toString());

        assertThat(result.getEndToEndIdentification()).isEqualTo(((LinkedHashMap) payment).get("endToEndIdentification"));
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getDebtorAccount().getIban()).isEqualTo(null);
        assertThat(result.getDebtorAccount().getCurrency()).isEqualTo(null);
        assertThat(result.getUltimateDebtor()).isNotNull();
        assertThat(result.getInstructedAmount()).isNotNull();
        assertThat(result.getCreditorAccount()).isNotNull();
        assertThat(result.getCreditorAgent()).isNotNull();
        assertThat(StringUtils.isNotBlank(result.getCreditorName())).isFalse();
        assertThat(result.getCreditorAddress()).isNotNull();
        assertThat(result.getPurposeCode()).isNotNull();
        assertThat(result.getRemittanceInformationStructured()).isNotNull();
        assertThat(result.getUltimateCreditor()).isNotBlank();
        assertThat(result.getRemittanceInformationUnstructured()).isNotBlank();
        assertThat(result.getDebtorAccount()).isNotNull();
        assertThat(result.getRequestedExecutionDate()).isNotNull();
        assertThat(result.getRequestedExecutionTime()).isNotNull();
    }

    @Test
    public void mapToCurrency() {
        //When
        Currency result = PaymentModelMapper.mapToCurrency(CURRENCY);
        //Then
        assertThat(result).isEqualTo(Currency.getInstance(CURRENCY));
    }

    @Test
    public void mapToXs2aAmount() {
        //Given
        Object request = getAmount12(true, true);
        //When
        Amount amount = PaymentModelMapper.mapToXs2aAmount(request);
        //Then
        assertThat(StringUtils.isNotBlank(amount.getContent())).isTrue();
        assertThat(amount.getCurrency()).isEqualTo(Currency.getInstance(CURRENCY));
    }

    @Test
    public void mapToXs2aAmount_null_should_not_fail() {
        //Given
        Object request = getAmount12(false, false);
        //When
        Amount amount = PaymentModelMapper.mapToXs2aAmount(request);
        //Then
        assertThat(StringUtils.isNotBlank(amount.getContent())).isFalse();
        assertThat(amount.getCurrency()).isNull();
    }

    @Test
    public void mapToXs2aAddress() {
        //Given
        Object address = getAddress12(true, true, true, true, true);
        //When
        Address result = PaymentModelMapper.mapToXs2aAddress(address);
        //Then
        assertThat(result.getStreet()).isNotBlank();
        assertThat(result.getBuildingNumber()).isNotBlank();
        assertThat(result.getCity()).isNotBlank();
        assertThat(result.getCountry()).isNotNull();
        assertThat(result.getPostalCode()).isNotBlank();
    }

    @Test
    public void mapToXs2aAddress_all_null_dont_fall() {
        //Given
        Object address = getAddress12(false, false, false, false, false);
        //When
        Address result = PaymentModelMapper.mapToXs2aAddress(address);
        //Then
        System.out.println(result.toString());
        assertThat(StringUtils.isNotBlank(result.getStreet())).isFalse();
        assertThat(result.getBuildingNumber()).isNotBlank();
        assertThat(result.getCity()).isNotBlank();
        assertThat(result.getCountry()).isNotNull();
        assertThat(result.getPostalCode()).isNotBlank();
    }

    @Test
    public void mapToXs2aAccountReference() {
        //Given
        Object request = getAccountReference12(true, true);
        //When
        AccountReference result = PaymentModelMapper.mapToXs2aAccountReference(request);
        //Then
        assertThat(StringUtils.isNotBlank(result.getIban())).isTrue();
        assertThat(result.getCurrency()).isEqualTo(Currency.getInstance(CURRENCY));
    }

    @Test
    public void mapToXs2aAccountReference_null_should_not_fail() {
        //Given
        Object request = getAccountReference12(false, false);
        //When
        AccountReference result = PaymentModelMapper.mapToXs2aAccountReference(request);
        //Then
        assertThat(StringUtils.isNotBlank(result.getIban())).isFalse();
        assertThat(result.getCurrency()).isNull();
    }

    @Test
    public void mapToXs2aBICFI() {
        //Given
        String bicfi = "Some test data";
        //When
        BICFI result = PaymentModelMapper.mapToXs2aBICFI(bicfi);
        //Then
        assertThat(result.getCode()).isEqualTo(bicfi);
    }

    //Static test data
    private LinkedHashMap<String, Object> getSinglePayment(boolean id, boolean acc, boolean amount, boolean agent, boolean creditorName, boolean credAddres, boolean remitance) {
        LinkedHashMap<String, Object> payment = new LinkedHashMap<>();
        payment.put("endToEndIdentification", id ? PAYMENT_ID : null);
        payment.put("debtorAccount", acc ? getAccountReference12(true, true) : null);
        payment.put("instructedAmount", amount ? getAmount12(true, true) : null);
        payment.put("creditorAccount", getAccountReference12(true, true));
        payment.put("creditorAgent", agent ? "Agent" : null);
        payment.put("creditorName", creditorName ? "CreditorName" : null);
        payment.put("creditorAddress", credAddres ? getAddress12(true, true, true, true, true) : null);
        payment.put("remittanceInformationUnstructured", remitance ? "some pmnt info" : null);
        return payment;
    }

    private LinkedHashMap<String, Object> getAddress12(boolean code, boolean str, boolean bld, boolean city, boolean country) {
        LinkedHashMap<String, Object> address = new LinkedHashMap<>();
        address.put("postalCode", code ? "PostalCode" : null);
        address.put("city", city ? "Kiev" : null);
        address.put("buildingNumber", bld ? "8" : null);
        address.put("street", str ? "Esplanadnaya" : null);
        address.put("country", country ? "Ukraine" : null);
        return address;
    }

    private LinkedHashMap<String, Object> getAmount12(boolean currency, boolean toPay) {
        LinkedHashMap<String, Object> instructedAmount = new LinkedHashMap<>();
        instructedAmount.put("currency", currency ? "EUR" : null);
        instructedAmount.put("amount", toPay ? "123456" : null);
        return instructedAmount;
    }

    private LinkedHashMap<String, Object> getAccountReference12(boolean iban, boolean currency) {
        LinkedHashMap<String, Object> ref = new LinkedHashMap<>();
        ref.put("iban", iban ? IBAN : null);
        ref.put("currency", currency ? CURRENCY : null);
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
        response.setTransactionStatus(TransactionStatus.ACCP);
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

    private Amount getXs2aAmount() {
        Amount amount = new Amount();
        amount.setContent(AMOUNT);
        amount.setCurrency(EUR);
        return amount;
    }


}
