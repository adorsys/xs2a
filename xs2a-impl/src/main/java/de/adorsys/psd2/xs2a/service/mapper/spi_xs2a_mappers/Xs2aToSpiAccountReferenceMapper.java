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


import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Xs2aToSpiAccountReferenceMapper {

    public List<SpiAccountReference> mapToSpiAccountReferences(List<AccountReference> references) {
        if (CollectionUtils.isEmpty(references)) {
            return Collections.emptyList();
        }
        return references.stream()
                   .map(ref -> Optional.ofNullable(ref).map(this::mapToSpiAccountReference).orElse(null))
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    public List<SpiAccountReference> mapToSpiAccountReferencesOrDefault(List<AccountReference> references, List<SpiAccountReference> defaultValue) {
        if (references == null) {
            return defaultValue;
        }

        return mapToSpiAccountReferences(references);
    }

    public SpiAccountReference mapToSpiAccountReference(AccountReference account) {
        if (account == null) {
            return null;
        }
        return new SpiAccountReference(
            account.getAspspAccountId(),
            account.getResourceId(),
            account.getIban(),
            account.getBban(),
            account.getPan(),
            account.getMaskedPan(),
            account.getMsisdn(),
            account.getCurrency(),
            account.getOther());
    }
}
