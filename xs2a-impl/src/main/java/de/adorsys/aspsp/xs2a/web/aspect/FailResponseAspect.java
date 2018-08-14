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

package de.adorsys.aspsp.xs2a.web.aspect;

import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.MessageService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class FailResponseAspect {
    private final MessageService messageService;

    @AfterReturning(pointcut = "execution(public * de.adorsys.aspsp.xs2a.service.*.*(..))", returning = "result")
    public Object invokeAspect(Object result) {
        return ResponseObject.class.isInstance(result)
                   ? enrichResponseObject(result)
                   : result;
    }

    private ResponseObject enrichResponseObject(Object result) {
        ResponseObject response = (ResponseObject) result;
        return response.hasError()
                   ? doEnrich(response)
                   : response;
    }

    private ResponseObject doEnrich(ResponseObject response) {
        MessageError error = response.getError();
        TppMessageInformation tppMessage = error.getTppMessage();
        tppMessage.setText(messageService.getMessage(tppMessage.getMessageErrorCode().name()));
        error.addTppMessage(tppMessage);
        return ResponseObject.builder()
                   .fail(error)
                   .build();
    }
}
