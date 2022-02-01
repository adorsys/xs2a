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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Xs2aObjectMapperTest {
    private static final String FIELD_NAME = "someField";
    private static final String FIELD_VALUE = "some value";
    private static final String NOT_EXISTING_FIELD_NAME = "unknownField";
    private static final String NULL_FIELD_NAME = "nullField";
    private static final String ARRAY_FIELD_NAME = "arrayField";
    private static final String NESTED_FIELD_NAME = "nestedField";
    private static final String NESTED_FIELD_VALUE_1 = "nested value 1";
    private static final String NESTED_FIELD_VALUE_2 = "nested value 2";
    private static final String TEST_OBJECT_JSON_PATH = "/json/mapper/test-object.json";
    private static final String MALFORMED_OBJECT_PATH = "/json/mapper/non-json.txt";

    @Test
    void toJsonField() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        Optional<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonField(resourceStream, FIELD_NAME, new TypeReference<String>() {
            });
        }

        // Then
        assertTrue(actualValue.isPresent());
        assertEquals(FIELD_VALUE, actualValue.get());
    }

    @Test
    void toJsonField_unknownField() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        Optional<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonField(resourceStream, NOT_EXISTING_FIELD_NAME, new TypeReference<String>() {
            });
        }

        // Then
        assertFalse(actualValue.isPresent());
    }

    @Test
    void toJsonField_nullValue() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        Optional<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonField(resourceStream, NULL_FIELD_NAME, new TypeReference<String>() {
            });
        }

        // Then
        assertFalse(actualValue.isPresent());
    }

    @Test
    void toJsonField_wrongType() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        Optional<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonField(resourceStream, ARRAY_FIELD_NAME, new TypeReference<String>() {
            });
        }

        // Then
        assertFalse(actualValue.isPresent());
    }

    @Test
    void toJsonField_malformedObject() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        Optional<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(MALFORMED_OBJECT_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonField(resourceStream, FIELD_NAME, new TypeReference<String>() {
            });
        }

        // Then
        assertFalse(actualValue.isPresent());
    }

    @Test
    void toJsonGetValuesForField() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();
        List<String> expectedValue = Arrays.asList(NESTED_FIELD_VALUE_1, NESTED_FIELD_VALUE_2);

        List<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonGetValuesForField(resourceStream, NESTED_FIELD_NAME);
        }

        // Then
        assertEquals(expectedValue, actualValue);
    }

    @Test
    void toJsonGetValuesForField_unknownField() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        List<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(TEST_OBJECT_JSON_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonGetValuesForField(resourceStream, NOT_EXISTING_FIELD_NAME);
        }

        // Then
        assertTrue(actualValue.isEmpty());
    }

    @Test
    void toJsonGetValuesForField_malformedObject() throws IOException {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        List<String> actualValue;
        try (InputStream resourceStream = getFileAsStream(MALFORMED_OBJECT_PATH)) {
            // When
            actualValue = xs2aObjectMapper.toJsonGetValuesForField(resourceStream, NESTED_FIELD_NAME);
        }


        // Then
        assertTrue(actualValue.isEmpty());
    }

    private InputStream getFileAsStream(String path) {
        return this.getClass().getResourceAsStream(path);
    }

    @Test
    void copy() {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        // When
        ObjectMapper copy = xs2aObjectMapper.copy();

        // Then
        assertTrue(copy instanceof Xs2aObjectMapper);
    }
}
