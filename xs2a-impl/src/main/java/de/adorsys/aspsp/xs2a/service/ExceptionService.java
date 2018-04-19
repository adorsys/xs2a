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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.FORMAT_ERROR;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Service
@AllArgsConstructor
public class ExceptionService {
    private final MessageService messageService;

    public ResponseObject<Amount> getAmount(boolean exc) {
        if (exc) {
            return new ResponseObject<>(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)
                                                         .text(messageService.getMessage(FORMAT_ERROR.name()))));
        }
        Amount amount = new Amount();
        amount.setContent("Some content");
        amount.setCurrency(Currency.getInstance("EUR"));
        return new ResponseObject<>(amount);
    }
}
