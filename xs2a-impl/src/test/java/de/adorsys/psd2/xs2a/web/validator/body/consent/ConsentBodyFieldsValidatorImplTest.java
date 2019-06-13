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
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConsentBodyFieldsValidatorImplTest {
    private static final String ACCESS_FIELD = "access";
    private static final String DESERIALIZATION_ERROR = "Cannot deserialize the request body";

    private HttpServletRequest request;
    private ConsentBodyFieldsValidatorImpl validator;
    private Consents consents;
    private MessageError messageError;
    private JsonReader jsonReader = new JsonReader();

    @Mock
    private JsonConverter jsonConverter;
    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.empty());

        consents = jsonReader.getObjectFromFile("json/validation/ais/consents.json", Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        messageError = new MessageError();

        byte[] requestContent = jsonReader.getBytesFromFile("json/validation/ais/consents.json");
        this.request = buildRequestWithContent(requestContent);

        validator = new ConsentBodyFieldsValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400), objectMapper, jsonConverter);
    }

    @Test
    public void validate_success() {
        // Given
        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "allAccounts");
        accessMap.put("allPsd2", "allAccounts");

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_allPsd2_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", "allAccounts");

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_availableAccounts_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "allAccounts");

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_availableAccountsWithBalances_success() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalances", "allAccounts");

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq(ACCESS_FIELD), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    public void validate_recurringIndicator_null_error() {
        consents.setRecurringIndicator(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'recurringIndicator' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_validUntil_null_error() {
        consents.setValidUntil(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'validUntil' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_validUntil_inPast_error() {
        consents.setValidUntil(LocalDate.now().minusDays(1));

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'validUntil' should not be in the past", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_null_error() {
        consents.setFrequencyPerDay(null);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be null", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_is0_error() {
        consents.setFrequencyPerDay(0);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be lower than 1", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_frequencyPerDay_lessThen1_error() {
        consents.setFrequencyPerDay(-1);

        validator.validate(request, messageError);
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Value 'frequencyPerDay' should not be lower than 1", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_availableAccounts_invalidValue_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", "Accounts");

        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for availableAccounts", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_availableAccounts_invalidType_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccounts-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccounts", 1);

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for availableAccounts", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_allPsd2_invalidValue_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", "AllAccounts");

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for allPsd2", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_allPsd2_invalidType_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-allPsd2-invalidType.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("allPsd2", 1);

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for allPsd2", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_availableAccountsWithBalances_invalidValue_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances-invalidValue.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalances", "Accounts");

        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for availableAccountsWithBalances", messageError.getTppMessage().getText());
    }

    @Test
    public void validate_availableAccountsWithBalances_invalidType_error() throws IOException {
        // Given
        String jsonFilePath = "json/validation/ais/consents-availableAccountsWithBalances-invalidType.json";
        consents = jsonReader.getObjectFromFile(jsonFilePath, Consents.class);
        when(objectMapper.readValue(any(InputStream.class), eq(Consents.class)))
            .thenReturn(consents);

        Map<String, Object> accessMap = new HashMap<>();
        accessMap.put("availableAccountsWithBalances", 1);

        // noinspection unchecked
        when(jsonConverter.toJsonField(any(InputStream.class), eq("access"), any(TypeReference.class)))
            .thenReturn(Optional.of(accessMap));

        // When
        validator.validate(request, messageError);

        // Then
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals("Wrong value for availableAccountsWithBalances", messageError.getTppMessage().getText());
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
        assertEquals(MessageErrorCode.FORMAT_ERROR, messageError.getTppMessage().getMessageErrorCode());
        assertEquals(DESERIALIZATION_ERROR, messageError.getTppMessage().getText());
    }

    private HttpServletRequest buildRequestWithContent(byte[] content) {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setContent(content);
        return mockRequest;
    }
}
