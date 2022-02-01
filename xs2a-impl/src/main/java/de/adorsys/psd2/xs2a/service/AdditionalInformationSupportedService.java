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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.core.ais.AccountAccessType.ALL_ACCOUNTS_WITH_OWNER_NAME;

@Service
@RequiredArgsConstructor
public class AdditionalInformationSupportedService {
    private final AspspProfileServiceWrapper aspspProfileService;

    public CreateConsentReq checkIfAdditionalInformationSupported(CreateConsentReq request) {
        boolean isOwnerNameSupported = aspspProfileService.isAccountOwnerInformationSupported();
        boolean isTrustedBeneficiariesSupported = aspspProfileService.isTrustedBeneficiariesSupported();

        if (!isOwnerNameSupported) {
            clearAccountAccessType(request);
        }

        if (!isOwnerNameSupported || !isTrustedBeneficiariesSupported) {
            AccountAccess access = request.getAccess();
            AdditionalInformationAccess additionalInformationAccess = access.getAdditionalInformationAccess();
            if (additionalInformationAccess != null) {
                AdditionalInformationAccess additionalInformationCleaned = new AdditionalInformationAccess(
                    isOwnerNameSupported ? additionalInformationAccess.getOwnerName() : null,
                    isTrustedBeneficiariesSupported ? additionalInformationAccess.getTrustedBeneficiaries() : null);
                request.setAccess(new AccountAccess(access.getAccounts(),
                                                    access.getBalances(),
                                                    access.getTransactions(),
                                                    additionalInformationCleaned));
            }
        }

        return request;
    }

    private CreateConsentReq clearAccountAccessType(CreateConsentReq request) {
        if (request.getAvailableAccounts() == ALL_ACCOUNTS_WITH_OWNER_NAME) {
            request.setAvailableAccounts(ALL_ACCOUNTS);
        }
        if (request.getAvailableAccountsWithBalance() == ALL_ACCOUNTS_WITH_OWNER_NAME) {
            request.setAvailableAccountsWithBalance(ALL_ACCOUNTS);
        }
        if (request.getAllPsd2() == ALL_ACCOUNTS_WITH_OWNER_NAME) {
            request.setAllPsd2(ALL_ACCOUNTS);
        }

        return request;
    }
}
