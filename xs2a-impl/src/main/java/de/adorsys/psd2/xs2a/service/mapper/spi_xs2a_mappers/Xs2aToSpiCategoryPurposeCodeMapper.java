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

import de.adorsys.psd2.core.payment.model.CategoryPurposeCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiCategoryPurposeCode;
import org.springframework.stereotype.Component;

@Component
public class Xs2aToSpiCategoryPurposeCodeMapper {

    public SpiCategoryPurposeCode mapToSpiCategoryPurposeCode(CategoryPurposeCode categoryPurposeCode) {
        if (categoryPurposeCode == null) {
            return null;
        }

        return SpiCategoryPurposeCode.valueOf(categoryPurposeCode.name());
    }

    public CategoryPurposeCode mapToCategoryPurposeCode(SpiCategoryPurposeCode spiCategoryPurposeCode) {
        if (spiCategoryPurposeCode == null) {
            return null;
        }

        return CategoryPurposeCode.valueOf(spiCategoryPurposeCode.name());
    }
}