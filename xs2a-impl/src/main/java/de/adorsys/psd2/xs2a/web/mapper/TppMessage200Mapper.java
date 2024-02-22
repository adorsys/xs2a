/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.MessageCode200InitiationStatus;
import de.adorsys.psd2.model.TppMessageCategory;
import de.adorsys.psd2.model.TppMessageInitiationStatusResponse200;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import org.apache.commons.collections.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TppMessage200Mapper {

    default List<TppMessageInitiationStatusResponse200> mapToTppMessage200List(Set<TppMessageInformation> tppMessages) {
        if (CollectionUtils.isEmpty(tppMessages)) {
            return null; //NOSONAR
        }
        return tppMessages.stream()
                   .map(this::mapToTppMessage200)
                   .collect(Collectors.toList());
    }

    @Mapping(target = "category", expression = "java(mapToTppMessageCategory(tppMessage.getCategory()))")
    @Mapping(target = "code", expression = "java(getMessageCode())")
    TppMessageInitiationStatusResponse200 mapToTppMessage200(TppMessageInformation tppMessage);

    TppMessageCategory mapToTppMessageCategory(MessageCategory messageCategory);

    default MessageCode200InitiationStatus getMessageCode() {
        return MessageCode200InitiationStatus.FUNDS_NOT_AVAILABLE;
    }
}
