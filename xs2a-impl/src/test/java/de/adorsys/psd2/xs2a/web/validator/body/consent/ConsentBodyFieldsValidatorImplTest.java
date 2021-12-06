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

package de.adorsys.psd2.xs2a.web.validator.body.consent;

import com.fasterxml.jackson.core.type.TypeReference;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.DateFieldValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.TppRedirectUriBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.raw.FieldExtractor;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aRequestBodyDateFields;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentBodyFieldsValidatorImplTest {
    private static final String ACCESS_FIELD = "access";
    private static final MessageError VALID_UNTIL_DATE_WRONG_VALUE_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "validUntil", "ISO_DATE", "YYYY-MM-DD"));

    private HttpServletRequest request;
    private ConsentBodyFieldsValidatorImpl validator;
    private Consents consents;
    private MessageError messageError;
    private final JsonReader jsonReader = new JsonReader();

    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private TppRedirectUriBodyValidatorImpl tppRedirectUriBodyValidator;
    @Mock
    private DateFieldValidator dateFieldValidator;
    @Mock
    private FieldExtractor fieldExtractor;

    @BeforeEach
    void setUp() {
        messageError = new MessageError(ErrorType.AIS_400);

        byte[] requestContent = jsonReader.getBytesFromFile("json/validation/ais/consents.json");
        this.request = buildRequestWithContent(requestContent);

        ErrorBuildingService errorBuildingServiceMock = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        validator =
            new ConsentBodyFieldsValidatorImpl(errorBuildingServiceMock, xs2aObjectMapper, tppRedirectUriBodyValidator,
                                               dateFieldValidator, fieldExtractor,
                                               new FieldLengthValidator(errorBuildingServiceMock));
    }

    @Test
    void validate_success() throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

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

    @ParameterizedTest
    @MethodSource("paramsForSuccess")
    void validate_differentAccesses_success(String path, String access) throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        consents = jsonReader.getObjectFromFile(path, Consents.class);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put(access, "allAccounts");

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    private static Stream<Arguments> paramsForSuccess() {
        return Stream.of(Arguments.arguments("json/validation/ais/consents-allPsd2.json", "allPsd2"),
                         Arguments.arguments("json/validation/ais/consents-availableAccounts.json", "availableAccounts"),
                         Arguments.arguments("json/validation/ais/consents-availableAccountsWithBalances.json", "availableAccountsWithBalance")
        );
    }

    @Test
    void validate_recurringIndicator_null_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setRecurringIndicator(null);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"recurringIndicator"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    void validate_validUntil_null_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setValidUntil(null);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"validUntil"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    void validate_validUntil_inPast_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setValidUntil(LocalDate.now().minusDays(1));
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_DATE_IN_THE_PAST, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"validUntil"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    void validate_frequencyPerDay_null_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setFrequencyPerDay(null);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{"frequencyPerDay"}, messageError.getTppMessage().getTextParameters());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    void validate_frequencyPerDay_is0_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setFrequencyPerDay(0);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FREQUENCY, messageError.getTppMessage().getMessageErrorCode());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @Test
    void validate_frequencyPerDay_lessThen1_error() throws IOException {
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        consents.setFrequencyPerDay(-1);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR_INVALID_FREQUENCY, messageError.getTppMessage().getMessageErrorCode());
        verify(tppRedirectUriBodyValidator, times(1)).validate(request, messageError);
    }

    @ParameterizedTest
    @MethodSource("paramsForError")
    void validate_differentAccesses_error(String access, String value) throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put(access, value);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{access}, messageError.getTppMessage().getTextParameters());
    }

    private static Stream<Arguments> paramsForError() {
        return Stream.of(Arguments.arguments("availableAccounts", "Accounts"),
                         Arguments.arguments("allPsd2", "AllAccounts"),
                         Arguments.arguments("availableAccountsWithBalance", "Accounts")
        );
    }

    @ParameterizedTest
    @MethodSource("paramsForInvalidType")
    void validate_invalidType_error(String access) throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put(access, 1);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_VALUE, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new String[]{access}, messageError.getTppMessage().getTextParameters());
    }

    private static Stream<Arguments> paramsForInvalidType() {
        return Stream.of(Arguments.arguments("availableAccounts"),
                         Arguments.arguments("allPsd2"),
                         Arguments.arguments("availableAccountsWithBalance")
        );
    }

    @Test
    void validate_exceptionOnGettingInputStream_error() throws IOException {
        // Given
        HttpServletRequest malformedRequest = mock(HttpServletRequest.class);

        when(dateFieldValidator.validateDateFormat(malformedRequest, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        when(malformedRequest.getInputStream())
            .thenThrow(new IOException());

        // When
        validator.validate(malformedRequest, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR_DESERIALIZATION_FAIL, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_validUntilDateWrongValue_wrongFormat_error() throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(new MessageError(ErrorType.AIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_WRONG_FORMAT_DATE_FIELD, "validUntil", "ISO_DATE", "YYYY-MM-DD")));

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        // When
        MessageError actualError = validator.validate(request, messageError);

        // Then
        assertEquals(VALID_UNTIL_DATE_WRONG_VALUE_ERROR, actualError);
    }

    @Test
    void validate_validUntilCorrectValue_success() throws IOException {
        // Given
        when(dateFieldValidator.validateDateFormat(request, Xs2aRequestBodyDateFields.AIS_CONSENT_DATE_FIELDS.getDateFields(), messageError))
            .thenReturn(messageError);

        // noinspection unchecked
        when(xs2aObjectMapper.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        when(fieldExtractor.mapBodyToInstance(request, messageError, Consents.class)).thenReturn(Optional.of(consents));

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
