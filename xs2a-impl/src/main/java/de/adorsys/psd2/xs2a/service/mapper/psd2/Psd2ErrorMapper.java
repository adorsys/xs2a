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

package de.adorsys.psd2.xs2a.service.mapper.psd2;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public abstract class Psd2ErrorMapper<T, R> {
    @Autowired
    protected MessageService messageService;

    public abstract Function<T, R> getMapper();

    public abstract HttpStatus getErrorStatus();

    protected String getErrorText(TppMessageInformation tppMessageInformation) {
        String messageInformationText = tppMessageInformation.getText();
        return StringUtils.isBlank(messageInformationText)
                   ? messageService.getMessage(tppMessageInformation.getMessageErrorCode().name())
                   : messageInformationText;
    }
}
