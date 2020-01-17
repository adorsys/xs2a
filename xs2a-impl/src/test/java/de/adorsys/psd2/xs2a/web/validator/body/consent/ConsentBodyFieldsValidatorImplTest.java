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

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.converter.LocalDateConverter;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentBodyFieldsValidatorImplTest {
    private static final String ACCESS_FIELD = "access";
    private static final String VALID_UNTIL_FIELD_NAME = "validUntil";
    private static final String CORRECT_FORMAT_DATE = "2021-10-10";
    private static final String WRONG_FORMAT_DATE = "07/01/2019 00:00:00";

    private static final MessageError VALID_UNTIL_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "validUntil", "ISO_DATE", "YYYY-MM-DD"));

    private HttpServletRequest request;
    private ConsentBodyFieldsValidatorImpl validator;
    private Consents consents;
    private MessageError messageError;
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    private FieldExtractor fieldExtractor;

    @Before
    public void setUp() throws IOException {
        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        when(xs2aObjectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        fieldExtractor = new FieldExtractor(errorService, xs2aObjectMapper);
        messageError = new MessageError(ErrorType.AIS_400);

        byte[] requestContent = jsonReader.getBytesFromFile("json/validation/ais/consents.json");
        this.request = buildRequestWithContent(requestContent);

        validator = new ConsentBodyFieldsValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400), xs2aObjectMapper,
                                                       tppRedirectUriBodyValidator,
                                                       new DateFieldValidator(errorService, new LocalDateConverter(), fieldExtractor), fieldExtractor);
    }

    @Test
    public void validate_success() {
        // Given
        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "allAccounts");
        accessMap.put("allPsd2", "allAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_allPsd2_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(xs2aObjectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", "allAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_availableAccounts_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(xs2aObjectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "allAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_availableAccountsWithBalance_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(xs2aObjectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalance", "allAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_recurringIndicator_null_error() {
        consents.setRecurringIndicator(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"recurringIndicator"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_validUntil_null_error() {
        consents.setValidUntil(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"validUntil"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_validUntil_inPast_error() {
        consents.setValidUntil(LocalDate.now().minusDays(1));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_DATE_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"validUntil"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_frequencyPerDay_null_error() {
        consents.setFrequencyPerDay(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"frequencyPerDay"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_frequencyPerDay_is0_error() {
        consents.setFrequencyPerDay(0);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FREQUENCY, messageError.getTppMessage().getMessageErrorCode());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_frequencyPerDay_lessThen1_error() {
        consents.setFrequencyPerDay(-1);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FREQUENCY, messageError.getTppMessage().getMessageErrorCode());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    public void validate_availableAccounts_invalidValue_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "Accounts");

        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"availableAccounts"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_availableAccounts_invalidType_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", 1);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"availableAccounts"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_allPsd2_invalidValue_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", "AllAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"allPsd2"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_allPsd2_invalidType_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2-invalidType.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", 1);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"allPsd2"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_availableAccountsWithBalance_invalidValue_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalance", "Accounts");

        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"availableAccountsWithBalance"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_availableAccountsWithBalance_invalidType_error() {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances-invalidType.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalance", 1);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"availableAccountsWithBalance"}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    public void validate_exceptionOnGettingInputStream_error() throws IOException {
        // Given
        HttpServletRequest malformedRequest = mock(HttpServletRequest.class);
        when(malformedRequest.getInputStream())
            .thenThrow(new IOException());

        // When
        validator.validate(malformedRequest, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_DESERIALIZATION_FAIL, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    public void validate_validUntilDateWrongValue_wrongFormat_error() {
        // Given
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(VALID_UNTIL_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(WRONG_FORMAT_DATE));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(VALID_UNTIL_DATE_WRONG_VALUE_ERROR, messageError);
    }

    @Test
    public void validate_requestedExecutionDateCorrectValue_success() {
        // Given
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(VALID_UNTIL_FIELD_NAME), any(TypeReference.class))).thenReturn(Optional.of(CORRECT_FORMAT_DATE));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    private HttpServletRequest buildRequestWithContent(byte[] content) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(content);
        return mockRequest;
    }
}
