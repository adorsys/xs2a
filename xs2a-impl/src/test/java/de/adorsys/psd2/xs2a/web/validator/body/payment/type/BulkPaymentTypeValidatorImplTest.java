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

package de.adorsys.psd2.xs2a.web.validator.body.payment.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BulkPaymentTypeValidatorImplTest {

    private static final String VALUE_36_LENGHT = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJK";
    private static final String VALUE_71_LENGHT = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJ";

    private BulkPaymentTypeValidatorImpl validator;
    private MessageError messageError;

    private BulkPayment bulkPayment;
    private SinglePayment singlePayment;

    @Before
    public void setUp() {
        JsonReader jsonReader = new JsonReader();
        messageError = new MessageError();
        bulkPayment = jsonReader.getObjectFromFile("json/validation/bulk-payment.json", BulkPayment.class);
        bulkPayment.setRequestedExecutionDate(LocalDate.now().plusDays(1));
        assertTrue(CollectionUtils.isNotEmpty(bulkPayment.getPayments()));
        singlePayment = bulkPayment.getPayments().get(0);

        ObjectMapper objectMapper = new ObjectMapper();
        validator = new BulkPaymentTypeValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400),
                                                       objectMapper,
                                                       new PaymentMapper(objectMapper));
    }

    @Test
    public void getPaymentType() {
        assertEquals(PaymentType.BULK, validator.getPaymentType());
    }

    @Test
    public void doValidation_success() {
        validator.doBulkValidation(bulkPayment, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void doValidation_endToEndIdentification_tooLong_error() {
        singlePayment.setEndToEndIdentification(VALUE_36_LENGHT);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format("Value '%s' should not be more than %s symbols", "endToEndIdentification", 35),
                     messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_singleDebtorAccount_null_error() {
        singlePayment.setDebtorAccount(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'debtorAccount' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_instructedAmount_null_error() {
        singlePayment.setInstructedAmount(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'instructedAmount' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_instructedAmount_currency_null_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setCurrency(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'currency' has wrong format", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_instructedAmount_amount_null_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setAmount(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'amount' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_instructedAmount_amount_wrong_format_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setAmount(VALUE_71_LENGHT + VALUE_71_LENGHT);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'amount' has wrong format", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_creditorAccount_null_error() {
        singlePayment.setCreditorAccount(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'creditorAccount' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_creditorName_null_error() {
        singlePayment.setCreditorName(null);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'creditorName' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_creditorName_tooLong_error() {
        singlePayment.setCreditorName(VALUE_71_LENGHT);

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format("Value '%s' should not be more than %s symbols", "creditorName", 70),
                     messageError.getTppMessage().getText());
    }

    @Test
    public void doValidation_requestedExecutionDate_error() {
        singlePayment.setRequestedExecutionDate(LocalDate.now().minusDays(1));

        validator.doSingleValidation(singlePayment, messageError);
        assertEquals(MessageErrorCode.PERIOD_INVALID, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'requestedExecutionDate' should not be in the past", messageError.getTppMessage().getText());
    }
}
