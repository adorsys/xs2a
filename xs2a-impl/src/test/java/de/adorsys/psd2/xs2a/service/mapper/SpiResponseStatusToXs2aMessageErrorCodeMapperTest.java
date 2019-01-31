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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class SpiResponseStatusToXs2aMessageErrorCodeMapperTest {
    @InjectMocks
    private SpiResponseStatusToXs2aMessageErrorCodeMapper spiResponseStatusToXs2aMessageErrorCodeMapper;

    @Test
    public void mapToMessageErrorCode_UNAUTHORIZED_FAILURE() {
        //Given:
        //When:
        MessageErrorCode messageErrorCode = mapToMessageErrorCode(SpiResponseStatus.UNAUTHORIZED_FAILURE);
        //Then:
        assertNotNull(messageErrorCode);
        assertEquals(messageErrorCode, PSU_CREDENTIALS_INVALID);
    }

    @Test
    public void mapToMessageErrorCode_TECHNICAL_FAILURE() {
        //Given:
        //When:
        MessageErrorCode messageErrorCode = mapToMessageErrorCode(SpiResponseStatus.TECHNICAL_FAILURE);
        //Then:
        assertNotNull(messageErrorCode);
        assertEquals(messageErrorCode, INTERNAL_SERVER_ERROR);
    }

    @Test
    public void mapToMessageErrorCode_LOGICAL_FAILURE() {
        //Given:
        //When:
        MessageErrorCode messageErrorCode = mapToMessageErrorCode(SpiResponseStatus.LOGICAL_FAILURE);
        //Then:
        assertNotNull(messageErrorCode);
        assertEquals(messageErrorCode, FORMAT_ERROR);
    }

    @Test
    public void mapToMessageErrorCode_NOT_SUPPORTED() {
        //Given:
        //When:
        MessageErrorCode messageErrorCode = mapToMessageErrorCode(SpiResponseStatus.NOT_SUPPORTED);
        //Then:
        assertNotNull(messageErrorCode);
        assertEquals(messageErrorCode, PARAMETER_NOT_SUPPORTED);
    }

    private MessageErrorCode mapToMessageErrorCode(SpiResponseStatus spiResponseStatus) {
        return spiResponseStatusToXs2aMessageErrorCodeMapper.mapToMessageErrorCode(spiResponseStatus);
    }
}
