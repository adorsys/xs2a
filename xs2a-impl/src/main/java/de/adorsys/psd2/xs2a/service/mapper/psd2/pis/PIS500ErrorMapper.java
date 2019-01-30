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

package de.adorsys.psd2.xs2a.service.mapper.psd2.pis;

import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.exception.model.error500.Error500NGPIS;
import de.adorsys.psd2.xs2a.exception.model.error500.MessageCode500PIS;
import de.adorsys.psd2.xs2a.exception.model.error500.TppMessage500PIS;
import de.adorsys.psd2.xs2a.service.mapper.psd2.Psd2ErrorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PIS500ErrorMapper extends Psd2ErrorMapper<MessageError, Error500NGPIS> {

    @Override
    public Function<MessageError, Error500NGPIS> getMapper() {
        return this::mapToPsd2Error;
    }

    @Override
    public HttpStatus getErrorStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Error500NGPIS mapToPsd2Error(MessageError messageError) {
        return new Error500NGPIS().tppMessages(mapToTppMessage500PIS(messageError.getTppMessages()));
    }

    private List<TppMessage500PIS> mapToTppMessage500PIS(Set<TppMessageInformation> tppMessages) {
        return tppMessages.stream()
                   .map(m -> new TppMessage500PIS()
                                 .category(TppMessageCategory.fromValue(m.getCategory().name()))
                                 .code(MessageCode500PIS.fromValue(m.getMessageErrorCode().getName()))
                                 .path(m.getPath())
                                 .text(getErrorText(m))
                   ).collect(Collectors.toList());
    }
}
