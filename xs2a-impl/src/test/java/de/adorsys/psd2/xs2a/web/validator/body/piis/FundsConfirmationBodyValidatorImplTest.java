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

package de.adorsys.psd2.xs2a.web.validator.body.piis;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Amount;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AccountReferenceValidator;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.CurrencyValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundsConfirmationBodyValidatorImplTest {
    private MessageError messageError = new MessageError(ErrorType.PIIS_400);

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private AccountReferenceValidator accountReferenceValidator;
    @Mock
    private AmountValidator amountValidator;
    @Mock
    private CurrencyValidator currencyValidator;
    @Mock
    private FieldExtractor fieldExtractor;

    private FundsConfirmationBodyValidatorImpl fundsConfirmationBodyValidator;

    @BeforeEach
    void setUp() {
        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.PIIS_400);
        fundsConfirmationBodyValidator =
            new FundsConfirmationBodyValidatorImpl(errorBuildingServiceMock, xs2aObjectMapper, accountReferenceValidator,
                                                   amountValidator, currencyValidator, fieldExtractor,
                                                   new FieldLengthValidator(errorBuildingServiceMock));
    }

    @Test
    void validate_success() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        confirmationOfFunds.setAccount(new AccountReference());
        confirmationOfFunds.setInstructedAmount(new Amount());

        when(fieldExtractor.mapBodyToInstance(mockRequest, this.messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void validate_cardNumber_oversized() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(36));
        confirmationOfFunds.setAccount(new AccountReference());
        confirmationOfFunds.setInstructedAmount(new Amount());

        when(fieldExtractor.mapBodyToInstance(mockRequest, this.messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[]{"cardNumber", 35}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_instructedAmountIsNull_fail() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        confirmationOfFunds.setAccount(new AccountReference());

        when(fieldExtractor.mapBodyToInstance(mockRequest, messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        MessageError expected = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "instructedAmount"));
        assertEquals(expected, messageError);
    }

    @Test
    void validate_amountValidator_fail() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        confirmationOfFunds.setAccount(new AccountReference());
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        confirmationOfFunds.setInstructedAmount(amount);

        when(fieldExtractor.mapBodyToInstance(mockRequest, messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        doAnswer((Answer<Void>) invocation -> {
            TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "amount");
            messageError.addTppMessage(tppMessageInformation);
            return null;
        }).when(amountValidator).validateAmount(amount.getAmount(), messageError);

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        MessageError expected = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "amount"));
        assertEquals(expected, messageError);
    }

    @Test
    void validate_currencyValidator_fail() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        confirmationOfFunds.setAccount(new AccountReference());
        Amount amount = new Amount();
        amount.setAmount("100");
        confirmationOfFunds.setInstructedAmount(amount);

        when(fieldExtractor.mapBodyToInstance(mockRequest, messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        doAnswer((Answer<Void>) invocation -> {
            TppMessageInformation tppMessageInformation = TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, "currency");
            messageError.addTppMessage(tppMessageInformation);
            return null;
        }).when(currencyValidator).validateCurrency(amount.getCurrency(), messageError);

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        MessageError expected = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(FORMAT_ERROR_EMPTY_FIELD, "currency"));
        assertEquals(expected, messageError);
    }

    @Test
    void validate_accessIsNull_fail() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setAmount("10");
        confirmationOfFunds.setInstructedAmount(amount);

        when(fieldExtractor.mapBodyToInstance(mockRequest, messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        MessageError expected = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "access"));
        assertEquals(expected, messageError);
    }

    @Test
    void validate_accessValidator_fail() {
        //Given
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();

        ConfirmationOfFunds confirmationOfFunds = new ConfirmationOfFunds();
        confirmationOfFunds.setCardNumber(RandomStringUtils.random(35));
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setAmount("10");
        confirmationOfFunds.setInstructedAmount(amount);
        AccountReference accountReference = new AccountReference();
        accountReference.setIban("It's not iban");
        confirmationOfFunds.setAccount(accountReference);

        when(fieldExtractor.mapBodyToInstance(mockRequest, messageError, ConfirmationOfFunds.class))
            .thenReturn(Optional.of(confirmationOfFunds));

        TppMessageInformation tppMessageInformation = TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, "IBAN");
        doAnswer((Answer<Void>) invocation -> {
            messageError.addTppMessage(tppMessageInformation);
            return null;
        }).when(accountReferenceValidator).validate(accountReference, messageError);

        //When
        fundsConfirmationBodyValidator.validate(mockRequest, messageError);

        //Then
        MessageError expected = new MessageError(ErrorType.PIIS_400, tppMessageInformation);
        assertEquals(expected, messageError);
    }

}
