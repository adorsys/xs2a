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

import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class ResponseMapperTest {
    @InjectMocks
    private ResponseMapper responseMapper;

    @Test
    void contentTypeSet() {
        CustomContentTypeProvider responseWithContentType = () -> MediaType.APPLICATION_PDF;

        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject
                                                                       .<CustomContentTypeProvider>builder()
                                                                       .body(responseWithContentType)
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertEquals(MediaType.APPLICATION_PDF, responseEntity.getHeaders().getContentType());
    }

    @Test
    void contentTypeNotSet() {
        ResponseObject<String> responseObject = ResponseObject.<String>builder()
                                                   .body("some body")
                                                   .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertNull(responseEntity.getHeaders().getContentType());
    }
}
