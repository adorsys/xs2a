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

package de.adorsys.psd2.xs2a.web.validator.body.raw;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FieldExtractorTest {
    private static final String FIELD_NAME = "endToEndIdentification";
    private static final String FIELD_VALUE = "RI-123456789";
    private static final String CURRENCY_FIELD_NAME = "currency";
    private static final String MONTHS_OF_EXECUTION_FIELD_NAME = "monthsOfExecution";
    private static final String PERIODIC_PAYMENT_JSON_PATH = "json/validation/periodic-payment.json";
    private static final MessageError DESERIALIZATION_ERROR =
        new MessageError(ErrorType.PIS_400, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_DESERIALIZATION_FAIL));

    private FieldExtractor fieldExtractor;
    private MessageError messageError;
    private MockHttpServletRequest mockRequest;
    private HttpServletRequest mockedRequest;
    private JsonReader jsonReader = new JsonReader();

    @BeforeEach
    void setUp() throws IOException {
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        ErrorBuildingService errorService = new ErrorBuildingServiceMock(ErrorType.PIS_400);
        messageError = new MessageError(ErrorType.PIS_400);
        fieldExtractor = new FieldExtractor(errorService, xs2aObjectMapper);
        mockRequest = new MockHttpServletRequest();
        String body = jsonReader.getStringFromFile(PERIODIC_PAYMENT_JSON_PATH);
        mockRequest.setContent(body.getBytes(StandardCharsets.UTF_8));
        mockedRequest = Mockito.mock(HttpServletRequest.class);
        when(mockedRequest.getInputStream()).thenThrow(new IOException(""));
    }

    @Test
    void extractField_Success() {
        Optional<String> expectedResult = Optional.of(FIELD_VALUE);
        Optional<String> actualResult = fieldExtractor.extractField(mockRequest, FIELD_NAME, messageError);
        assertEquals(expectedResult, actualResult);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void extractField_Exception() {
        Optional<String> actualResult = fieldExtractor.extractField(mockedRequest, FIELD_NAME, messageError);
        assertFalse(actualResult.isPresent());
        assertEquals(DESERIALIZATION_ERROR, messageError);
    }

    @Test
    void extractOptionalField_Success() {
        Optional<String> expectedResult = Optional.of(FIELD_VALUE);
        Optional<String> actualResult = fieldExtractor.extractOptionalField(mockRequest, FIELD_NAME);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void extractOptionalField_Exception() {
        Optional<String> actualResult = fieldExtractor.extractOptionalField(mockedRequest, FIELD_NAME);
        assertFalse(actualResult.isPresent());
    }

    @Test
    void extractList_Success() {
        List<String> expectedResult = getMonthsOfExecutionList();
        List<String> actualResult = fieldExtractor.extractList(mockRequest, MONTHS_OF_EXECUTION_FIELD_NAME, messageError);
        assertEquals(expectedResult, actualResult);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void extractList_Exception() {
        List<String> actualResult = fieldExtractor.extractList(mockedRequest, FIELD_NAME, messageError);
        assertTrue(actualResult.isEmpty());
        assertEquals(DESERIALIZATION_ERROR, messageError);
    }

    @Test
    void extractOptionalList_Success() {
        List<String> expectedResult = getCurrencyList();
        List<String> actualResult = fieldExtractor.extractOptionalList(mockRequest, CURRENCY_FIELD_NAME);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void extractOptionalList_Exception() {
        List<String> actualResult = fieldExtractor.extractOptionalList(mockedRequest, FIELD_NAME);
        assertTrue(actualResult.isEmpty());
    }

    private List<String> getCurrencyList() {
        List<String> currencyList = new ArrayList<>();
        currencyList.add("EUR");
        currencyList.add("EUR");
        currencyList.add("EUR");
        return currencyList;
    }

    private List<String> getMonthsOfExecutionList() {
        List<String> monthsList = new ArrayList<>();
        monthsList.add("1");
        monthsList.add("2");
        monthsList.add("3");
        return monthsList;
    }
}
