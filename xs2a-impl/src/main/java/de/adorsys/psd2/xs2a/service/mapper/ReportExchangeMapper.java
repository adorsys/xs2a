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
