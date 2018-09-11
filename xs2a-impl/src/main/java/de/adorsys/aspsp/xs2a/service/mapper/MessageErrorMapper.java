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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.message.MessageService;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
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
                                 .map(str -> mapToGenericError(str, "n/a"))  //TODO add actual path
                                 .collect(Collectors.toList()))
                   .map(this::mapTppMessageGenericListToTppMessages)
                   .orElse(null);
    }

    public TppMessages mapToTppMessages(MessageError error) {
        return Optional.ofNullable(error)
                   .map(MessageError::getTppMessages)
                   .map(e -> e.stream()
                                 .map(this::mapToGenericError)
                                 .collect(Collectors.toList()))
                   .map(this::mapTppMessageGenericListToTppMessages)
                   .orElse(null);
    }

    private TppMessages mapTppMessageGenericListToTppMessages(List<TppMessageGeneric> messageGenericList) {
        return Optional.ofNullable(messageGenericList)
                   .map(c -> {
                       TppMessages messages = new TppMessages();
                       messages.addAll(c);
                       return messages;
                   })
                   .orElse(null);
    }

    private TppMessageGeneric mapToGenericError(TppMessageInformation info) {
        return Optional.ofNullable(info)
                   .map(i -> mapToGenericError(i.getMessageErrorCode(), i.getPath()))
                   .orElse(null);
    }

    private TppMessageGeneric mapToGenericError(MessageErrorCode code, String path) {
        return Optional.ofNullable(code)
                   .map(c -> {
                       TppMessageGeneric tppMessage = new TppMessageGeneric();
                       tppMessage.setCategory(TppMessageCategory.ERROR);
                       tppMessage.setCode(code);
                       tppMessage.setPath(path);
                       tppMessage.setText(messageService.getMessage(c.name()));
                       return tppMessage;
                   })
                   .orElse(null);
    }
}
