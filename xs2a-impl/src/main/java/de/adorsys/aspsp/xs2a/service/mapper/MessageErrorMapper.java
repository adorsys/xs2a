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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageErrorMapper {

    private final MessageService messageService;


    public TppMessages mapToTppMessages(MessageErrorCode... tppMessages) {
        return Optional.ofNullable(tppMessages)
                   .map(m -> Arrays.stream(m)
                                 .map(this::mapToGenericError)
                                 .collect(Collectors.toList()))
                   .map(c -> {
                       TppMessages messages = new TppMessages();
                       messages.addAll(c);
                       return messages;
                   })
                   .orElse(null);
    }

    public TppMessages mapToTppMessages(MessageError error) {
        return Optional.ofNullable(error)
                   .map(MessageError::getTppMessages)
                   .map(e -> e.stream()
                                 .map(this::mapToGenericError)
                                 .collect(Collectors.toList()))
                   .map(c -> {
                       TppMessages messages = new TppMessages();
                       messages.addAll(c);
                       return messages;
                   })
                   .orElse(null);
    }

    private TppMessageGeneric mapToGenericError(MessageErrorCode code) {
        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setCode(code);
        tppMessage.setPath("N/A"); //TODO set path
        tppMessage.setText(messageService.getMessage(code.name()));
        return tppMessage;
    }

    private TppMessageGeneric mapToGenericError(TppMessageInformation info) {
        MessageErrorCode code = info.getMessageErrorCode();

        TppMessageGeneric tppMessage = new TppMessageGeneric();
        tppMessage.setCategory(TppMessageCategory.ERROR);
        tppMessage.setPath(info.getPath());
        tppMessage.setCode(code);
        tppMessage.setText(messageService.getMessage(code.name()));

        return tppMessage;
    }
}
