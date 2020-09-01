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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationBulkElementJson;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.IbanValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.DefaultPaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class BulkPaymentTypeValidatorImplTest {
    private static final String VALUE_36_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJK";
    private static final String VALUE_71_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJ";
    private static final String VALUE_141_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJ";
    private final BulkPaymentInitiationJson BULK_PAYMENT_INITIATION_JSON = getBulkPaymentInitiationJson();

    private BulkPaymentTypeValidatorImpl validator;
    private MessageError messageError;

    private BulkPayment bulkPayment;
    private SinglePayment singlePayment;
    private PaymentValidationConfig validationConfig;

    @BeforeEach
    void setUp() {
        JsonReader jsonReader = new JsonReader();
        messageError = new MessageError();
        bulkPayment = jsonReader.getObjectFromFile("json/validation/bulk-payment.json", BulkPayment.class);
        bulkPayment.setRequestedExecutionDate(LocalDate.now().plusDays(1));
        assertTrue(CollectionUtils.isNotEmpty(bulkPayment.getPayments()));
        singlePayment = bulkPayment.getPayments().get(0);
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        PurposeCodeMapper purposeCodeMapper = Mappers.getMapper(PurposeCodeMapper.class);
        RemittanceMapper remittanceMapper = Mappers.getMapper(RemittanceMapper.class);
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.AIS_400);

        validationConfig = new DefaultPaymentValidationConfigImpl();

        validator = new BulkPaymentTypeValidatorImpl(errorBuildingServiceMock,
                                                     xs2aObjectMapper,
                                                     new PaymentMapper(xs2aObjectMapper, purposeCodeMapper),
                                                     new AmountValidator(errorBuildingServiceMock),
                                                     new IbanValidator(errorBuildingServiceMock));
    }

    @Test
    void getPaymentType() {
        assertEquals(PaymentType.BULK, validator.getPaymentType());
    }

    @Test
    void doValidation_success() {
        validator.validate(BULK_PAYMENT_INITIATION_JSON, messageError, validationConfig);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void doValidation_IllegalArgumentException() {
        Object body = new Object();
        validator.validate(body, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void doValidation_IllegalArgumentException_extraField() {
        PeriodicPayment body = new PeriodicPayment();
        validator.validate(body, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"paymentId"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doBulkValidation_success() {
        validator.doBulkValidation(bulkPayment, messageError, validationConfig);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void doValidation_endToEndIdentification_tooLong_error() {
        singlePayment.setEndToEndIdentification(VALUE_36_LENGTH);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"endToEndIdentification", 35}, messageError.getTppMessage().getTextParameters());

    }

    @Test
    void doValidation_instructionIdentification_tooLong_error() {
        singlePayment.setInstructionIdentification(VALUE_36_LENGTH);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"instructionIdentification", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_bulkDebtorAccount_null_error() {
        bulkPayment.setDebtorAccount(null);

        validator.doBulkValidation(bulkPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"debtorAccount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_bulkPayments_null_error() {
        bulkPayment.setPayments(Collections.emptyList());

        validator.doBulkValidation(bulkPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_BULK, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void doValidation_bulkDateInThePast_error() {
        bulkPayment.setRequestedExecutionDate(LocalDate.MIN);

        validator.doBulkValidation(bulkPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.EXECUTION_DATE_INVALID_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void doValidation_singleDebtorAccount_null_error() {
        singlePayment.setDebtorAccount(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"debtorAccount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_null_error() {
        singlePayment.setInstructedAmount(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"instructedAmount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_currency_null_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setCurrency(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"currency"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_amount_null_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setAmount(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"amount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_amount_wrong_format_error() {
        Xs2aAmount instructedAmount = singlePayment.getInstructedAmount();
        instructedAmount.setAmount(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"amount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorAccount_null_error() {
        singlePayment.setCreditorAccount(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorAccount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorName_null_error() {
        singlePayment.setCreditorName(null);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorName"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorName_tooLong_error() {
        singlePayment.setCreditorName(VALUE_71_LENGTH);

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorName", 70}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_requestedExecutionDate_error() {
        singlePayment.setRequestedExecutionDate(LocalDate.now().minusDays(1));

        validator.doSingleValidation(singlePayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.EXECUTION_DATE_INVALID_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void doBulkValidation_remittanceInformationStructuredArray_reference_error() {
        singlePayment.setRemittanceInformationStructuredArray(Collections.singletonList(VALUE_141_LENGTH));

        validator.doBulkValidation(bulkPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"remittanceInformationStructured", 140}, messageError.getTppMessage().getTextParameters());
    }

    private BulkPaymentInitiationJson getBulkPaymentInitiationJson() {
        BulkPaymentInitiationJson bulkPaymentInitiationJson = new BulkPaymentInitiationJson();
        bulkPaymentInitiationJson.setDebtorAccount(new AccountReference());
        PaymentInitiationBulkElementJson paymentInitiationBulkElementJson = new PaymentInitiationBulkElementJson();
        paymentInitiationBulkElementJson.setCreditorName("name");
        paymentInitiationBulkElementJson.setCreditorAccount(new AccountReference());
        Amount amount = new Amount();
        amount.setAmount("100");
        amount.setCurrency("EUR");
        paymentInitiationBulkElementJson.setInstructedAmount(amount);
        bulkPaymentInitiationJson.setPayments(Collections.singletonList(paymentInitiationBulkElementJson));
        return bulkPaymentInitiationJson;
    }
}
