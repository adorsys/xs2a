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

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus.*;

@Component
public class SpiResponseStatusToXs2aMessageErrorCodeMapper {
    private static final Map<SpiResponseStatus, MessageErrorCode> spiResponseStatusToMessageErrorCode;

    static {    // TODO discuss error mapping https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
        spiResponseStatusToMessageErrorCode = new HashMap<>();
        spiResponseStatusToMessageErrorCode.put(TECHNICAL_FAILURE, INTERNAL_SERVER_ERROR);
        spiResponseStatusToMessageErrorCode.put(UNAUTHORIZED_FAILURE, PSU_CREDENTIALS_INVALID);
        spiResponseStatusToMessageErrorCode.put(LOGICAL_FAILURE, FORMAT_ERROR);
        spiResponseStatusToMessageErrorCode.put(NOT_SUPPORTED, PARAMETER_NOT_SUPPORTED);
    }

    public MessageErrorCode mapToMessageErrorCode(SpiResponseStatus responseStatus) {
        return spiResponseStatusToMessageErrorCode.getOrDefault(responseStatus, INTERNAL_SERVER_ERROR);
    }

    public String mapToTppMessage(List<String> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            return String.join(", ", messages);
        }
        return null;
    }
}
