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

import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpiErrorMapper {
    public ErrorHolder mapToErrorHolder(SpiResponse<?> spiResponse, ServiceType serviceType) {
        List<TppMessage> errors = spiResponse.getErrors();

        if (errors.isEmpty()) {
            throw new IllegalArgumentException("SPI response must contain errors for mapping");
        }

        TppMessage firstTppMessage = errors.get(0);

        TppMessageInformation[] tppMessages = errors.stream()
                                                  .map(this::mapToMessageError)
                                                  .toArray(TppMessageInformation[]::new);

        ErrorType errorType = ErrorType.getByServiceTypeAndErrorCode(serviceType, firstTppMessage.getErrorCode().getCode())
                                  .orElse(null);

        return ErrorHolder.builder(errorType).tppMessages(tppMessages).build();
    }

    private TppMessageInformation mapToMessageError(TppMessage tppMessage) {
        if (StringUtils.isBlank(tppMessage.getMessageText())) {
            if (ArrayUtils.isNotEmpty(tppMessage.getMessageTextArgs())) {
                return TppMessageInformation.of(tppMessage.getErrorCode(), tppMessage.getMessageTextArgs());
            } else {
                return TppMessageInformation.of(tppMessage.getErrorCode());
            }
        }
        return TppMessageInformation.buildWithCustomError(tppMessage.getErrorCode(), tppMessage.getMessageText());
    }
}
