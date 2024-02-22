/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiMessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.error.SpiTppMessage;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiErrorMapper {
    public ErrorHolder mapToErrorHolder(SpiResponse<?> spiResponse, ServiceType serviceType) {
        List<SpiTppMessage> spiErrors = spiResponse.getErrors();

        if (spiErrors.isEmpty()) {
            throw new IllegalArgumentException("SPI response must contain errors for mapping");
        }

        List<TppMessage> errors = spiErrors.stream()
                                      .map(this::mapToTppMessage)
                                      .collect(Collectors.toList());

        TppMessage firstTppMessage = errors.get(0);

        TppMessageInformation[] tppMessages = errors.stream()
                                                  .map(this::mapToMessageError)
                                                  .toArray(TppMessageInformation[]::new);

        ErrorType errorType = ErrorType.getByServiceTypeAndErrorCode(serviceType, firstTppMessage.getErrorCode().getCode())
                                  .orElse(null);

        return ErrorHolder.builder(errorType).tppMessages(tppMessages).build();
    }

    private TppMessage mapToTppMessage(SpiTppMessage spiTppMessage) {
        if (spiTppMessage == null) {
            return null;
        }

        MessageErrorCode messageErrorCode = mapToMessageErrorCode(spiTppMessage.getErrorCode());
        return new TppMessage(messageErrorCode, spiTppMessage.getMessageText(), spiTppMessage.getMessageTextArgs());
    }

    private MessageErrorCode mapToMessageErrorCode(SpiMessageErrorCode errorCode) {
        return errorCode == null
                   ? null
                   : MessageErrorCode.valueOf(errorCode.name());
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
