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

package de.adorsys.psd2.xs2a.service.event.mapper;

import de.adorsys.psd2.event.service.model.PsuIdDataBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    default PsuIdDataBO toEventPsuIdData(de.adorsys.psd2.xs2a.core.psu.PsuIdData xs2aPsuIdData) {
        if (xs2aPsuIdData == null) {
            return null;
        }
        return new PsuIdDataBO(xs2aPsuIdData.getPsuId(),
                               xs2aPsuIdData.getPsuIdType(),
                               xs2aPsuIdData.getPsuCorporateId(),
                               xs2aPsuIdData.getPsuCorporateIdType());
    }
}
