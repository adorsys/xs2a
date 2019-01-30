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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpiErrorMapper {
    private final SpiResponseStatusToXs2aMessageErrorCodeMapper spiToXs2aMessageErrorCodeMapper;
    private final SpiResponseToServiceAndErrorTypeMapper spiToServiceAndErrorTypeMapper;

    public ErrorHolder mapToErrorHolder(SpiResponse<?> spiResponse, ServiceType serviceType) {
        SpiResponseStatus responseStatus = spiResponse.getResponseStatus();
        return ErrorHolder.builder(spiToXs2aMessageErrorCodeMapper.mapToMessageErrorCode(responseStatus))
                   .errorType(spiToServiceAndErrorTypeMapper.mapToErrorType(responseStatus, serviceType))
                   .messages(spiResponse.getMessages())
                   .build();
    }
}
