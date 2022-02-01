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

package de.adorsys.psd2.xs2a.config.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingJackson2TextMessageConverterTest {
    @Test
    void checkSupportedMediaTypes() {
        ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);
        MappingJackson2TextMessageConverter messageConverter = new MappingJackson2TextMessageConverter(mockedObjectMapper);
        List<MediaType> supportedMediaTypes = messageConverter.getSupportedMediaTypes();

        assertEquals(1, supportedMediaTypes.size());
        assertTrue(supportedMediaTypes.contains(MediaType.TEXT_PLAIN));
    }
}
