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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring",
    uses = {SpiToXs2aAccountReferenceMapper.class, SpiToXs2aBalanceMapper.class})
public interface SpiToXs2aBalanceReportMapper {

    @Mapping(target = "balances", source = "balances")
    @Mapping(target = "xs2aAccountReference", source = "accountReference")
    Xs2aBalancesReport mapToXs2aBalancesReportSpi(SpiAccountReference accountReference, List<SpiAccountBalance> balances);

    @Mapping(target = "balances", source = "balances")
    @Mapping(target = "xs2aAccountReference", source = "accountReference")
    Xs2aBalancesReport mapToXs2aBalancesReport(AccountReference accountReference, List<SpiAccountBalance> balances);

}
