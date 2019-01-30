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

import de.adorsys.psd2.model.Error405NGPIS;
import de.adorsys.psd2.model.MessageCode405PIS;
import de.adorsys.psd2.model.TppMessage405PIS;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.Psd2ErrorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PIS405ErrorMapper extends Psd2ErrorMapper<MessageError, Error405NGPIS> {

    @Override
    public Function<MessageError, Error405NGPIS> getMapper() {
        return this::mapToPsd2Error;
    }

    @Override
    public HttpStatus getErrorStatus() {
        return HttpStatus.METHOD_NOT_ALLOWED;
    }

    private Error405NGPIS mapToPsd2Error(MessageError messageError) {
        return new Error405NGPIS().tppMessages(mapToTppMessage405PIS(messageError.getTppMessages()));
    }

    private List<TppMessage405PIS> mapToTppMessage405PIS(Set<TppMessageInformation> tppMessages) {
        return tppMessages.stream()
                   .map(m -> new TppMessage405PIS()
                                 .category(TppMessageCategory.fromValue(m.getCategory().name()))
                                 .code(MessageCode405PIS.fromValue(m.getMessageErrorCode().getName()))
                                 .path(m.getPath())
                                 .text(getErrorText(m))
                   ).collect(Collectors.toList());
    }
}
