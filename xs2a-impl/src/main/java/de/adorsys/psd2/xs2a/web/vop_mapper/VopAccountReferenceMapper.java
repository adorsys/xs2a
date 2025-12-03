/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.vop_mapper;

import de.adorsys.psd2.model.AccountReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

/**
 * Specialized mapper for VOP AccountReference objects.
 * Handles the mapping of AccountReference from API layer (with Optional fields) 
 * to internal domain model.
 */
@Mapper(componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
public interface VopAccountReferenceMapper {

    /**
     * Maps API AccountReference to internal AccountReference.
     * MapStruct automatically handles unwrapping of Optional fields.
     */
    @Mapping(target = "aspspAccountId", ignore = true)
    @Mapping(target = "resourceId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "other", source = "other.identification")
    de.adorsys.psd2.xs2a.core.profile.AccountReference mapToXs2aAccountReference(AccountReference accountReference);
}
