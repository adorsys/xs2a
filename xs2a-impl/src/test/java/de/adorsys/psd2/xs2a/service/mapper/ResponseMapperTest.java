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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ResponseMapperTest {
    @Mock
    private MessageErrorMapper messageErrorMapper;

    @InjectMocks
    private ResponseMapper responseMapper;

    @Test
    public void contentTypeSet() {
        CustomContentTypeProvider responseWithContentType = () -> MediaType.APPLICATION_PDF;

        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject
                                                                       .<CustomContentTypeProvider>builder()
                                                                       .body(responseWithContentType)
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertEquals("When response object is CustomContentTypeProvider, use custom ContentType",
                     MediaType.APPLICATION_PDF, responseEntity.getHeaders().getContentType());
    }

    @Test
    public void contentTypeNotSet() {
        ResponseObject<String> responseObject = ResponseObject.<String>builder()
                                                   .body("some body")
                                                   .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertNull("When response object is not CustomContentTypeProvider, ContentType not set explicitly",
                     responseEntity.getHeaders().getContentType());
    }
}
