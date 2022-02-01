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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.mapper.config.ObjectMapperConfig;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.IbanValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.DefaultPaymentValidationConfigImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.service.CustomPaymentValidationService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PeriodicPaymentTypeValidatorImplTest {
    private static final String VALUE_36_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJK";
    private static final String VALUE_71_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJ";
    private static final String VALUE_141_LENGTH = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJKQWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJ";

    private PeriodicPaymentTypeValidatorImpl validator;
    private MessageError messageError;

    private PeriodicPayment periodicPayment;
    private AccountReference accountReference;
    private Xs2aAddress address;
    private PaymentValidationConfig validationConfig;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @BeforeEach
    void setUp() {
        JsonReader jsonReader = new JsonReader();
        messageError = new MessageError();
        periodicPayment = jsonReader.getObjectFromFile("json/validation/periodic-payment.json", PeriodicPayment.class);
        periodicPayment.setStartDate(LocalDate.now().plusDays(1));
        periodicPayment.setEndDate(LocalDate.now().plusDays(5));

        accountReference = jsonReader.getObjectFromFile("json/validation/account_reference.json", AccountReference.class);
        address = jsonReader.getObjectFromFile("json/validation/address.json", Xs2aAddress.class);

        Xs2aObjectMapper xs2aObjectMapper = new ObjectMapperConfig().xs2aObjectMapper();
        PurposeCodeMapper purposeCodeMapper = Mappers.getMapper(PurposeCodeMapper.class);
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.AIS_400);

        validationConfig = new DefaultPaymentValidationConfigImpl();

        validator = new PeriodicPaymentTypeValidatorImpl(errorBuildingServiceMock,
                                                         xs2aObjectMapper,
                                                         new PaymentMapper(xs2aObjectMapper, purposeCodeMapper),
                                                         new AmountValidator(errorBuildingServiceMock),
                                                         new IbanValidator(aspspProfileService, errorBuildingServiceMock),
                                                         new CustomPaymentValidationService(),
                                                         new FieldLengthValidator(errorBuildingServiceMock),
                                                         aspspProfileService);
    }

    @Test
    void getPaymentType() {
        assertEquals(PaymentType.PERIODIC, validator.getPaymentType());
    }

    @Test
    void doValidation_success() {
        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void doValidation_endToEndIdentification_tooLong_error() {
        periodicPayment.setEndToEndIdentification(VALUE_36_LENGTH);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"endToEndIdentification", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructionIdentification_tooLong_error() {
        periodicPayment.setInstructionIdentification(VALUE_36_LENGTH);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"instructionIdentification", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_debtorAccount_null_error() {
        periodicPayment.setDebtorAccount(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"debtorAccount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_null_error() {
        periodicPayment.setInstructedAmount(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"instructedAmount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_currency_null_error() {
        Xs2aAmount instructedAmount = periodicPayment.getInstructedAmount();
        instructedAmount.setCurrency(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"currency"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_amount_null_error() {
        Xs2aAmount instructedAmount = periodicPayment.getInstructedAmount();
        instructedAmount.setAmount(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"amount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_instructedAmount_amount_wrong_format_error() {
        Xs2aAmount instructedAmount = periodicPayment.getInstructedAmount();
        instructedAmount.setAmount(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"amount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorAccount_null_error() {
        periodicPayment.setCreditorAccount(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorAccount"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorName_null_error() {
        periodicPayment.setCreditorName(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorName"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_creditorName_tooLong_error() {
        periodicPayment.setCreditorName(VALUE_71_LENGTH);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"creditorName", 70}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_requestedExecutionDate_error() {
        periodicPayment.setRequestedExecutionDate(LocalDate.now().minusDays(1));

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.EXECUTION_DATE_INVALID_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validateAccount_success() {
        validator.validateAccount(accountReference, messageError, validationConfig);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validateAccount_iban_error() {
        accountReference.setIban("123");

        validator.validateAccount(accountReference, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"IBAN"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validateAccount_bban_error() {
        accountReference.setBban("123");

        validator.validateAccount(accountReference, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"BBAN"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_pan_tooLong_error() {
        accountReference.setPan(VALUE_36_LENGTH);

        validator.validateAccount(accountReference, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"PAN", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_maskedPan_tooLong_error() {
        accountReference.setMaskedPan(VALUE_36_LENGTH);

        validator.validateAccount(accountReference, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"Masked PAN", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_Msisdn_tooLong_error() {
        accountReference.setMsisdn(VALUE_36_LENGTH);

        validator.validateAccount(accountReference, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"MSISDN", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_success() {
        validator.validateAddress(address, messageError, validationConfig);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validatorAddress_street_tooLong_error() {
        address.setStreetName(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"streetName", 100}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_buildingNumber_tooLong_error() {
        address.setBuildingNumber(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"buildingNumber", 20}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_city_tooLong_error() {
        address.setTownName(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"townName", 100}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_postCode_tooLong_error() {
        address.setPostCode(VALUE_71_LENGTH + VALUE_71_LENGTH);

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"postCode", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_country_null_error() {
        address.setCountry(null);

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_VALUE_REQUIRED, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"address.country"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_country_codeBlank_error() {
        address.getCountry().setCode("");

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_VALUE_REQUIRED, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"address.country"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_country_codeFormat_error() {
        address.getCountry().setCode("zz");

        validator.validateAddress(address, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_ADDRESS_COUNTRY_INCORRECT, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validatorAddress_startDate_null_error() {
        periodicPayment.setStartDate(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"startDate"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_startDate_beforeNow_error() {
        periodicPayment.setStartDate(LocalDate.now().minusDays(1));

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_DATE_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"startDate"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_frequency_null_error() {
        periodicPayment.setFrequency(null);

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"frequency"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validatorAddress_paymentPeriod_error() {
        periodicPayment.setStartDate(LocalDate.now().plusDays(2)); // Start date bigger than end date
        periodicPayment.setEndDate(LocalDate.now().plusDays(1));

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.PERIOD_INVALID_WRONG_ORDER, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void doValidation_ultimate_debtor_error() {
        periodicPayment.setUltimateDebtor(VALUE_71_LENGTH);

        validator.doSingleValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"ultimateDebtor", 70}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_ultimate_creditor_error() {
        periodicPayment.setUltimateCreditor(VALUE_71_LENGTH);

        validator.doSingleValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"ultimateCreditor", 70}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doValidation_remittance_reference_error() {
        periodicPayment.setRemittanceInformationStructured(VALUE_141_LENGTH);

        validator.doSingleValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"remittanceInformationStructured", 140}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void doPeriodicValidation_remittanceInformationStructuredArray_reference_error() {
        periodicPayment.setRemittanceInformationStructuredArray(Collections.singletonList(VALUE_141_LENGTH));

        validator.doPeriodicValidation(periodicPayment, messageError, validationConfig);
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"remittanceInformationStructured", 140}, messageError.getTppMessage().getTextParameters());
    }
}
