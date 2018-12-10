/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JsonConverterServiceTest {
    @Spy
    private ObjectMapper objectMapper;
    @InjectMocks
    private JsonConverterService jsonConverterService;

    @Test
    public void encodeAndDecode_Success_AsciiString() {
        // Given
        String expected = "Some string";

        // When
        byte[] bytes = jsonConverterService.toJsonBytes(expected).get();
        String decoded = jsonConverterService.toObject(bytes, String.class).get();

        // Then
        assertThat(decoded).isEqualTo(expected);
    }

    @Test
    public void encodeAndDecode_Success_NullString() {
        // Given
        String expected = null;

        // When
        byte[] bytes = jsonConverterService.toJsonBytes(expected)
                           .orElse(null);
        String decoded = jsonConverterService.toObject(bytes, String.class)
                             .orElse(null);

        // Then
        assertThat(decoded).isEqualTo(expected);
    }

    @Test
    public void encodeAndDecode_Success_NonAsciiString() {
        // Given
        String nonAsciiString = "Maël Hörz köpa 名可名非常名";

        // When
        byte[] bytes = jsonConverterService.toJsonBytes(nonAsciiString).get();
        String decoded = jsonConverterService.toObject(bytes, String.class).get();

        // Then
        assertThat(decoded).isEqualTo(nonAsciiString);
    }

    @Test
    public void encodeAndDecode_Success_ObjectWithNonAsciiString() {
        // Given
        String nonAsciiString = "Maël Hörz köpa";
        String nonAsciiStringSecond = "名可名非常名";
        HolderObject expectedHolder = new HolderObject(nonAsciiString,
                                                       Collections.singletonMap(nonAsciiString, nonAsciiStringSecond));
        // When
        byte[] bytes = jsonConverterService.toJsonBytes(expectedHolder).get();
        HolderObject decodedHolder = jsonConverterService.toObject(bytes, HolderObject.class).get();

        // Then
        assertThat(decodedHolder).isEqualTo(expectedHolder);
    }

    @Value
    private static class HolderObject {
        private String someField;
        private Map<String, String> someMap;
    }
}
