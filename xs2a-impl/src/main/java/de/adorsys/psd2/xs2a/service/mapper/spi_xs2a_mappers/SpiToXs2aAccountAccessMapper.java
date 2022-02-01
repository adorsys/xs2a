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

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAdditionalInformationAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
public class SpiToXs2aAccountAccessMapper {
    private final SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;

    public Optional<AccountAccess> mapToAccountAccess(SpiAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(aa ->
                            new AccountAccess(
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getAccounts()),
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getBalances()),
                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(aa.getTransactions()),
                                mapToAdditionalInformationAccess(aa.getSpiAdditionalInformationAccess())));
    }

    public AccountAccess getAccessForGlobalOrAllAvailableAccountsConsent(CreateConsentReq request) {
        return new AccountAccess(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            modifyAdditionalInformationAccessOnGlobalOrAllAvailableAccountsConsent(request.getAccess().getAdditionalInformationAccess())
        );
    }

    private AdditionalInformationAccess modifyAdditionalInformationAccessOnGlobalOrAllAvailableAccountsConsent(AdditionalInformationAccess info) {
        if (info == null || info.noAdditionalInformationAccess()) {
            return null;
        }

        UnaryOperator<List<AccountReference>> modifier = list -> list == null ? null : Collections.emptyList();

        return new AdditionalInformationAccess(modifier.apply(info.getOwnerName()), modifier.apply(info.getTrustedBeneficiaries()));
    }

    private AdditionalInformationAccess mapToAdditionalInformationAccess(SpiAdditionalInformationAccess spiAdditionalInformationAccess) {
        return Optional.ofNullable(spiAdditionalInformationAccess)
                   .map(info -> new AdditionalInformationAccess(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(info.getOwnerName()),
                                                                spiToXs2aAccountReferenceMapper.mapToXs2aAccountReferences(info.getTrustedBeneficiaries())))
                   .orElse(null);
    }
}
