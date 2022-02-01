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
