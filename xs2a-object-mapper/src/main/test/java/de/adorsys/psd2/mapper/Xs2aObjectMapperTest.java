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

package de.adorsys.psd2.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class Xs2aObjectMapperTest {
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
    public void toJsonField() throws IOException {
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
    public void toJsonField_unknownField() throws IOException {
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
    public void toJsonField_nullValue() throws IOException {
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
    public void toJsonField_wrongType() throws IOException {
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
    public void toJsonField_malformedObject() throws IOException {
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
    public void toJsonGetValuesForField() throws IOException {
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
    public void toJsonGetValuesForField_unknownField() throws IOException {
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
    public void toJsonGetValuesForField_malformedObject() throws IOException {
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
    public void copy() {
        // Given
        Xs2aObjectMapper xs2aObjectMapper = new Xs2aObjectMapper();

        // When
        ObjectMapper copy = xs2aObjectMapper.copy();

        // Then
        assertTrue(copy instanceof Xs2aObjectMapper);
    }
}
