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

package de.adorsys.psd2.xs2a.web.error;

import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TppErrorMessageBuilder {
    private final MessageService messageService;

    public TppErrorMessage buildTppErrorMessage(MessageCategory messageCategory, MessageErrorCode messageErrorCode) {
        String text = messageService.getMessage(messageErrorCode.name());
        return new TppErrorMessage(messageCategory, messageErrorCode, text);
    }

    public TppErrorMessage buildTppErrorMessageWithPlaceholder(MessageCategory messageCategory, MessageErrorCode messageErrorCode, String placeholder) {
        String text = String.format(messageService.getMessage(messageErrorCode.name()), placeholder);
        return new TppErrorMessage(messageCategory, messageErrorCode, text);
    }
}
