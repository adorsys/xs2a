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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification;
import de.adorsys.psd2.xs2a.spi.domain.vop.SpiFinancialInstitutionIdentification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting FinancialInstitutionIdentification to SpiFinancialInstitutionIdentification.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiFinancialInstitutionIdentificationMapper {

    private final Xs2aToSpiClearingSystemMemberIdentificationMapper clearingSystemMemberIdentificationMapper;
    private final Xs2aToSpiPostalAddressMapper postalAddressMapper;
    private final Xs2aToSpiGenericFinancialInstitutionIdentificationMapper genericFinancialInstitutionIdentificationMapper;

    /**
     * Maps FinancialInstitutionIdentification to SpiFinancialInstitutionIdentification.
     *
     * @param source the XS2A FinancialInstitutionIdentification to map
     * @return the mapped SpiFinancialInstitutionIdentification
     */
    public SpiFinancialInstitutionIdentification mapToSpiFinancialInstitutionIdentification(
            FinancialInstitutionIdentification source) {
        if (source == null) {
            return null;
        }

        SpiFinancialInstitutionIdentification target = new SpiFinancialInstitutionIdentification();
        target.setBicfi(source.getBicfi());
        target.setClearingSystemMemberId(
            clearingSystemMemberIdentificationMapper.mapToSpiClearingSystemMemberIdentification(source.getClearingSystemMemberId())
        );
        target.setName(source.getName());
        target.setPostalAddress(postalAddressMapper.mapToSpiPostalAddress(source.getPostalAddress()));
        target.setOther(genericFinancialInstitutionIdentificationMapper.mapToSpiGenericFinancialInstitutionIdentification(source.getOther()));

        return target;
    }
}
