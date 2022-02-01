/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.mapper.psd2.sb;

import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.exception.model.error500.Error500NGSB;
import de.adorsys.psd2.xs2a.exception.model.error500.TppMessage500SB;
import de.adorsys.psd2.xs2a.service.mapper.psd2.Psd2ErrorMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class SB500ErrorMapper extends Psd2ErrorMapper<MessageError, Error500NGSB> {

    @Override
    public Function<MessageError, Error500NGSB> getMapper() {
        return this::mapToPsd2Error;
    }

    @Override
    public HttpStatus getErrorStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Error500NGSB mapToPsd2Error(MessageError messageError) {
        return Error500NGSB.builder()
                   .tppMessages(mapToTppMessage500SB(messageError.getTppMessages()))
                   .build();
    }

    private List<TppMessage500SB> mapToTppMessage500SB(Set<TppMessageInformation> tppMessages) {
        return tppMessages.stream()
                   .map(m -> TppMessage500SB.builder()
                                 .category(TppMessageCategory.fromValue(m.getCategory().name()))
                                 .code(m.getMessageErrorCode().getName())
                                 .path(m.getPath())
                                 .text(getErrorText(m))
                                 .build()
                   ).collect(Collectors.toList());
    }
}
