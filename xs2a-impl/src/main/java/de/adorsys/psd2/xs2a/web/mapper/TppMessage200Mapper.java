/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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
        return MessageCode200InitiationStatus.AVAILABLE;
    }
}
