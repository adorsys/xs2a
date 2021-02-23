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
