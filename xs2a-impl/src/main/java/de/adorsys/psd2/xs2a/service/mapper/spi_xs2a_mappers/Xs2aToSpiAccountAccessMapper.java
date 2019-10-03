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

import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAdditionalInformationAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiAccountAccessMapper {
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;

    public SpiAccountAccess mapToAccountAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new SpiAccountAccess(
                                xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aa.getAccounts()),
                                xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aa.getBalances()),
                                xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aa.getTransactions()),
                                aa.getAvailableAccounts(),
                                aa.getAllPsd2(),
                                aa.getAvailableAccountsWithBalance(),
                                mapToSpiAdditionalInformationAccess(aa.getAdditionalInformationAccess())
                            )
                   )
                   .orElse(null);
    }

    private SpiAdditionalInformationAccess mapToSpiAdditionalInformationAccess(AdditionalInformationAccess additionalInformationAccess) {
        return Optional.ofNullable(additionalInformationAccess)
                   .map(info -> new SpiAdditionalInformationAccess(
                       xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferencesOrDefault(info.getOwnerName(), null),
                       xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferencesOrDefault(info.getOwnerAddress(), null)
                   ))
                   .orElse(null);
    }
}
