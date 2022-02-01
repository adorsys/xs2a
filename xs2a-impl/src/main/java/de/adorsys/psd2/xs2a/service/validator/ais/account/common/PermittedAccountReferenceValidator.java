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

package de.adorsys.psd2.xs2a.service.validator.ais.account.common;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermittedAccountReferenceValidator {
    private final AccountReferenceAccessValidator accountReferenceAccessValidator;

    public ValidationResult validate(AisConsent aisConsent, String accountId, boolean withBalance) {
        AccountAccess accountAccess = aisConsent.getAspspAccountAccesses();
        List<AccountReference> accountReferences = withBalance
                                                       ? accountAccess.getBalances()
                                                       : accountAccess.getAccounts();

        return accountReferenceAccessValidator.validate(aisConsent, accountReferences, accountId, aisConsent.getAisConsentRequestType());
    }
}
