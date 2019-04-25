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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountAccessValidatorImplTest {

    private static final String VALUE_36_LENGHT = "QWERTYUIOPQWERTYUIOPQWERTYUIOPDFGHJK";

    private HttpServletRequest request;
    private AccountAccessValidatorImpl validator;
    private Consents consents;
    private MessageError messageError;
    private JsonReader jsonReader;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();
        consents = jsonReader.getObjectFromFile("json/validation/consents.json", Consents.class);
        messageError = new MessageError();
        request = new MockHttpServletRequest();

        validator = createValidator(consents);
    }

    @Test
    public void validate_success() {
        validator.validate(request, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_access_null_error() {
        consents.setAccess(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'access' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_wrongIban_error() {
        consents.getAccess().getAccounts().get(0).setIban("123");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Invalid IBAN format", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_wrongBban_error() {
        consents.getAccess().getBalances().get(0).setBban("123");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Invalid BBAN format", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_pan_tooLong_error() {
        consents.getAccess().getBalances().get(0).setPan(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format("Value '%s' should not be more than %s symbols", "PAN", 35),
                     messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_maskedPan_tooLong_error() {
        consents.getAccess().getAccounts().get(0).setMaskedPan(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format("Value '%s' should not be more than %s symbols", "Masked PAN", 35),
                     messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_Msisdn_tooLong_error() {
        consents.getAccess().getTransactions().get(0).setMsisdn(VALUE_36_LENGHT);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(String.format("Value '%s' should not be more than %s symbols", "MSISDN", 35),
                     messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_currency_blank_error() {
        consents.getAccess().getBalances().get(0).setCurrency("");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Invalid currency code format", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_account_currency_wrongFormat_error() {
        consents.getAccess().getBalances().get(0).setCurrency("zzz");

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Invalid currency code format", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_allPsd2_error() {
        consents = jsonReader.getObjectFromFile("json/validation/consents-allPsd2.json", Consents.class);
        validator = createValidator(consents);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Consent object can not contain both list of accounts and the flag allPsd2 or availableAccounts",
                     messageError.getTppMessage().getText());
    }

    @Test
    public void validate_availableAccounts_error() {
        consents = jsonReader.getObjectFromFile("json/validation/consents-availableAccounts.json", Consents.class);
        validator = createValidator(consents);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Consent object can not contain both list of accounts and the flag allPsd2 or availableAccounts",
                     messageError.getTppMessage().getText());
    }

    private AccountAccessValidatorImpl createValidator(Consents consents) {
        return new AccountAccessValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400), new ObjectMapper()) {
            @SuppressWarnings("unchecked")
            @Override
            protected <T> Optional<T> mapBodyToInstance(HttpServletRequest request, MessageError messageError, Class<T> clazz) {
                assertEquals(Consents.class, clazz);
                return (Optional<T>) Optional.of(consents);
            }
        };
    }
}
