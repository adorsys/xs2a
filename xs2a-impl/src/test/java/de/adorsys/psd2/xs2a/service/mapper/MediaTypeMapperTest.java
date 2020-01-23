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

package de.adorsys.psd2.xs2a.service.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaTypeMapperTest {
    private MediaTypeMapper mediaTypeMapper = new MediaTypeMapper();

    @Test
    void mapToMediaType_withStandardContentType() {
        // When
        MediaType mediaType = mediaTypeMapper.mapToMediaType("application/json");

        // Then
        assertEquals(MediaType.APPLICATION_JSON, mediaType);
    }

    @Test
    void mapToMediaType_withCustomContentType() {
        // Given
        MediaType expectedMediaType = new MediaType("custom", "type");

        // When
        MediaType mediaType = mediaTypeMapper.mapToMediaType("custom/type");

        // Then
        assertEquals(expectedMediaType, mediaType);
    }

    @Test
    void mapToMediaType_withInvalidContentType() {
        // When
        assertThrows(InvalidMediaTypeException.class, () -> mediaTypeMapper.mapToMediaType("invalid"));
    }
}
