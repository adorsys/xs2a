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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.Balance;
import de.adorsys.psd2.model.BalanceList;
import de.adorsys.psd2.model.BalanceType;
import de.adorsys.psd2.xs2a.domain.Xs2aBalance;
import de.adorsys.psd2.xs2a.service.mapper.AmountModelMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AmountModelMapper.class, OffsetDateTimeMapper.class})
public abstract class BalanceMapper {
    @Autowired
    protected OffsetDateTimeMapper offsetDateTimeMapper;

    @Mapping(target = "balanceType", expression = "java(mapToBalanceType(balance.getBalanceType()))")
    @Mapping(target = "lastChangeDateTime", expression = "java(offsetDateTimeMapper.mapToOffsetDateTime(balance.getLastChangeDateTime()))")
    public abstract Balance mapToBalance(Xs2aBalance balance);

    public BalanceList mapToBalanceList(List<Xs2aBalance> balances) {
        BalanceList balanceList = null;

        if (CollectionUtils.isNotEmpty(balances)) {
            balanceList = new BalanceList();

            balanceList.addAll(balances.stream()
                                   .map(this::mapToBalance)
                                   .collect(Collectors.toList()));
        }

        return balanceList;
    }

    public BalanceType mapToBalanceType(de.adorsys.psd2.xs2a.domain.BalanceType balanceType) {
        if (balanceType == null) {
            return null;
        }
        return BalanceType.fromValue(balanceType.getValue());
    }
}
