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
