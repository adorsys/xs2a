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

import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageErrorMapper {
    private final MessageService messageService;

    public TppMessages mapToTppMessages(MessageErrorCode... errorCodes) {
        return Optional.ofNullable(errorCodes)
                   .map(m -> Arrays.stream(m)
                                 .map(str -> mapToGenericError(str, "N/A"))  //TODO add actual path https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/300
                                 .collect(Collectors.toList()))
                   .map(this::mapTppMessageGenericListToTppMessages)
                   .orElseGet(TppMessages::new);
    }

    public TppMessages mapToTppMessages(MessageError error) {
        return Optional.ofNullable(error)
                   .map(MessageError::getTppMessages)
                   .map(e -> e.stream()
                                 .map(this::mapToGenericError)
                                 .collect(Collectors.toList()))
                   .map(this::mapTppMessageGenericListToTppMessages)
                   .orElseGet(TppMessages::new);
    }

    private TppMessages mapTppMessageGenericListToTppMessages(@NonNull List<TppMessageGeneric> messageGenericList) {
        return messageGenericList.stream()
                   .collect(Collectors.toCollection(TppMessages::new));
    }

    private TppMessageGeneric mapToGenericError(@NonNull TppMessageInformation info) {
        return mapToGenericError(info.getMessageErrorCode(), info.getPath());

    }

    private TppMessageGeneric mapToGenericError(@NonNull MessageErrorCode code, String path) {
        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setCode(code);
        tppMessage.setPath(path);
        tppMessage.setText(messageService.getMessage(code.name()));
        return tppMessage;
    }
}
