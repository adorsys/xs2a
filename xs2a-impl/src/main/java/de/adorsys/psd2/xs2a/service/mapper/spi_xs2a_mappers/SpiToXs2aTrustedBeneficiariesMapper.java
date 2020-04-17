/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTrustedBeneficiaries;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SpiToXs2aTrustedBeneficiariesMapper {
    private final SpiToXs2aAccountReferenceMapper accountReferenceMapper;
    private final SpiToXs2aAddressMapper addressMapper;

    public Xs2aTrustedBeneficiaries mapToXs2aTrustedBeneficiaries(SpiTrustedBeneficiaries spiTrustedBeneficiaries) {
        return Optional.ofNullable(spiTrustedBeneficiaries)
            .map(tb -> new Xs2aTrustedBeneficiaries(
                tb.getTrustedBeneficiaryId(),
                accountReferenceMapper.mapToXs2aAccountReference(tb.getDebtorAccount()),
                accountReferenceMapper.mapToXs2aAccountReference(tb.getCreditorAccount()),
                tb.getCreditorAgent(),
                tb.getCreditorName(),
                tb.getCreditorAlias(),
                tb.getCreditorId(),
                addressMapper.mapToAddress(tb.getCreditorAddress())
            ))
            .orElse(null);
    }

    public List<Xs2aTrustedBeneficiaries> mapToXs2aTrustedBeneficiariesList(List<SpiTrustedBeneficiaries> spiTrustedBeneficiaries) {
        if (CollectionUtils.isEmpty(spiTrustedBeneficiaries)) {
            return new ArrayList<>();
        }

        return spiTrustedBeneficiaries.stream()
                   .map(this::mapToXs2aTrustedBeneficiaries)
                   .collect(Collectors.toList());
    }
}
