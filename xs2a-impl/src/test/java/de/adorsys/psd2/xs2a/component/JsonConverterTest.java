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

package de.adorsys.psd2.xs2a.component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JsonConverterTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectNode mockObjectNode;
    @Mock
    private TextNode mockTextNode;
    @Mock
    private InputStream mockInputStream;
    @Mock
    private JsonParser mockParser;

    @InjectMocks
    private JsonConverter jsonConverter;

    @Test
    public void toJsonField() throws IOException {
        String fieldName = "some field";
        String fieldValue = "some value";

        when(objectMapper.readTree(mockInputStream))
            .thenReturn(mockObjectNode);
        when(mockObjectNode.get(fieldName))
            .thenReturn(mockTextNode);
        when(objectMapper.treeAsTokens(mockTextNode))
            .thenReturn(mockParser);
        when(objectMapper.readValue(eq(mockParser), any(TypeReference.class)))
            .thenReturn(fieldValue);

        // When
        Optional<String> actualValue = jsonConverter.toJsonField(mockInputStream, fieldName, new TypeReference<String>() {
        });

        // Then
        verify(mockObjectNode).get(eq(fieldName));
        verify(objectMapper).readValue(eq(mockParser), any(TypeReference.class));

        assertTrue(actualValue.isPresent());
        assertEquals(fieldValue, actualValue.get());
    }

    @Test
    public void toJsonField_noSuchField() throws IOException {
        String fieldName = "some field";

        when(objectMapper.readTree(mockInputStream))
            .thenReturn(mockObjectNode);
        when(mockObjectNode.get(fieldName))
            .thenReturn(null);

        // When
        Optional<String> actualValue = jsonConverter.toJsonField(mockInputStream, fieldName, new TypeReference<String>() {
        });

        // Then
        verify(mockObjectNode).get(eq(fieldName));
        verify(objectMapper, never()).readValue(eq(mockParser), any(TypeReference.class));

        assertFalse(actualValue.isPresent());
    }

    @Test
    public void toJsonField_nullField() throws IOException {
        String fieldName = "some field";

        when(objectMapper.readTree(mockInputStream))
            .thenReturn(mockObjectNode);
        when(mockObjectNode.get(fieldName))
            .thenReturn(mockTextNode);
        when(objectMapper.treeAsTokens(mockTextNode))
            .thenReturn(mockParser);
        when(objectMapper.readValue(eq(mockParser), any(TypeReference.class)))
            .thenReturn(null);

        // When
        Optional<String> actualValue = jsonConverter.toJsonField(mockInputStream, fieldName, new TypeReference<String>() {
        });

        // Then
        verify(mockObjectNode).get(eq(fieldName));
        verify(objectMapper).readValue(eq(mockParser), any(TypeReference.class));

        assertFalse(actualValue.isPresent());
    }

    @Test
    public void toJsonField_readValueException() throws IOException {
        String fieldName = "some field";

        when(objectMapper.readTree(mockInputStream))
            .thenReturn(mockObjectNode);
        when(mockObjectNode.get(fieldName))
            .thenReturn(mockTextNode);
        when(objectMapper.treeAsTokens(mockTextNode))
            .thenReturn(mockParser);
        when(objectMapper.readValue(eq(mockParser), any(TypeReference.class)))
            .thenThrow(new IOException());

        // When
        Optional<String> actualValue = jsonConverter.toJsonField(mockInputStream, fieldName, new TypeReference<String>() {
        });

        // Then
        verify(mockObjectNode).get(eq(fieldName));
        verify(objectMapper).readValue(eq(mockParser), any(TypeReference.class));

        assertFalse(actualValue.isPresent());
    }
}
