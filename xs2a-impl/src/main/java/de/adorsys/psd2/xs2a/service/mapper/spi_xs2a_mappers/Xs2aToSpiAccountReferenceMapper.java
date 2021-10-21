/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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
