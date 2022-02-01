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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.ReportExchangeRate;
import de.adorsys.psd2.model.ReportExchangeRateList;
import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AmountModelMapper.class, PurposeCodeMapper.class, Xs2aAddressMapper.class, AspspProfileServiceWrapper.class})
public abstract class ReportExchangeMapper {
    protected ReportExchangeRateList mapToReportExchanges(List<Xs2aExchangeRate> xs2aExchangeRates) {
        if (CollectionUtils.isEmpty(xs2aExchangeRates)) {
            return null;
        }

        return xs2aExchangeRates.stream()
                   .map(this::mapToReportExchangeRate)
                   .collect(Collectors.toCollection(ReportExchangeRateList::new));
    }

    protected abstract ReportExchangeRate mapToReportExchangeRate(Xs2aExchangeRate xs2aExchangeRate);
}
