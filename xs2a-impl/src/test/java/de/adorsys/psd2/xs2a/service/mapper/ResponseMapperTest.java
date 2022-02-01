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

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ResponseMapperTest {
    @InjectMocks
    private ResponseMapper responseMapper;

    @Mock
    private Function function;

    @Test
    void contentTypeSet() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void contentTypeSet_error() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .fail(new MessageError())
                                                                       .build();

        assertThrows(IllegalArgumentException.class, () -> responseMapper.ok(responseObject));
    }

    @Test
    void contentTypeNotSet() {
        ResponseObject<String> responseObject = ResponseObject.<String>builder()
                                                    .body("some body")
                                                    .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject);

        assertThat(responseEntity.getHeaders().getContentType()).isNull();
    }

    @Test
    void contentTypeSet_ok() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject, function);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void contentTypeSet_withHeaders_ok() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.ok(responseObject, function, getResponseHeaders());

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseCreated() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.created(responseObject);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseCreated_ok() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.created(responseObject, function);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseCreated_withHeaders_ok() {
        CustomContentTypeProvider customContentTypeProvider = () -> MediaType.APPLICATION_PDF;

        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(customContentTypeProvider)
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.created(responseObject, getResponseHeaders());

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseCreated_withHeadersAndMappers_ok() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.created(responseObject, function, getResponseHeaders());

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseAccepted() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.accepted(responseObject);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseDeleted() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.delete(responseObject);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    @Test
    void responseDeleted_withMapper() {
        ResponseObject<CustomContentTypeProvider> responseObject = ResponseObject.<CustomContentTypeProvider>builder()
                                                                       .body(getCustomContentTypeProvider())
                                                                       .build();

        ResponseEntity responseEntity = responseMapper.delete(responseObject, function);

        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
    }

    private ResponseHeaders getResponseHeaders() {
        return ResponseHeaders.builder().build();
    }

    private CustomContentTypeProvider getCustomContentTypeProvider() {
        return () -> MediaType.APPLICATION_PDF;
    }
}
