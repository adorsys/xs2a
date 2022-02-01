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
