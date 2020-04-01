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
