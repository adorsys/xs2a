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

import de.adorsys.psd2.model.MessageCode2XX;
import de.adorsys.psd2.model.TppMessage2XX;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MessageErrorMapper {
    private final MessageService messageService;

    public List<TppMessage2XX> mapToTppMessages(MessageErrorCode... errorCodes) {
        return Optional.ofNullable(errorCodes)
                   .map(m -> Arrays.stream(m)
                                 .map(this::getTppMessage2XX)
                                 .collect(Collectors.toList()))
                   .orElse(Collections.emptyList());
    }

    public TppMessage2XX mapToTppMessage(MessageError error) {
        return Optional.ofNullable(error)
                   .map(e -> e.getTppMessage().getMessageErrorCode())
                   .map(this::getTppMessage2XX)
                   .orElseGet(TppMessage2XX::new);
    }

    private TppMessage2XX getTppMessage2XX(MessageErrorCode code) {
        TppMessage2XX tppMessage2XX = new TppMessage2XX();
        tppMessage2XX.setCategory(TppMessageCategory.ERROR);
        tppMessage2XX.setCode(MessageCode2XX.WARNING); // TODO create error mapper according to new version of specification 1.3 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/592
        tppMessage2XX.setPath("N/A"); //TODO add actual path https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/300
        tppMessage2XX.setText(messageService.getMessage(code.name()));
        return tppMessage2XX;
    }
}
