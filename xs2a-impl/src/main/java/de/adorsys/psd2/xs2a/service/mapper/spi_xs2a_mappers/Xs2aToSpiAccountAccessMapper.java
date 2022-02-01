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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAdditionalInformationAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiAccountAccessMapper {
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;

    public SpiAccountAccess mapToAccountAccess(AisConsent aisConsent) {
        return new SpiAccountAccess(
            xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aisConsent.getAccess().getAccounts()),
            xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aisConsent.getAccess().getBalances()),
            xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferences(aisConsent.getAccess().getTransactions()),
            aisConsent.getConsentData().getAvailableAccounts(),
            aisConsent.getConsentData().getAllPsd2(),
            aisConsent.getConsentData().getAvailableAccountsWithBalance(),
            mapToSpiAdditionalInformationAccess(aisConsent.getAccess().getAdditionalInformationAccess())
        );
    }

    private SpiAdditionalInformationAccess mapToSpiAdditionalInformationAccess(AdditionalInformationAccess additionalInformationAccess) {
        return Optional.ofNullable(additionalInformationAccess)
                   .map(info -> new SpiAdditionalInformationAccess(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferencesOrDefault(info.getOwnerName(), null),
                                                                   xs2aToSpiAccountReferenceMapper.mapToSpiAccountReferencesOrDefault(info.getTrustedBeneficiaries(), null)))
                   .orElse(null);
    }
}
