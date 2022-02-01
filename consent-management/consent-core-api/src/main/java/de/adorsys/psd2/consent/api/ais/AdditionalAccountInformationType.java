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

package de.adorsys.psd2.consent.api.ais;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public enum AdditionalAccountInformationType {
    DEDICATED_ACCOUNTS {
        @Override
        public List<AccountReference> getReferencesByType(List<AccountReference> accountReferences) {
            return accountReferences;
        }
    },
    ALL_AVAILABLE_ACCOUNTS {
        @Override
        public List<AccountReference> getReferencesByType(List<AccountReference> accountReferences) {
            return Collections.emptyList();
        }
    },
    NONE {
        @Override
        public List<AccountReference> getReferencesByType(List<AccountReference> accountReferences) {
            return null; //NOSONAR
        }
    };

    public static AdditionalAccountInformationType findTypeByList(List<?> list) {
        return Optional.ofNullable(list)
                   .map(l -> l.isEmpty()
                                 ? AdditionalAccountInformationType.ALL_AVAILABLE_ACCOUNTS
                                 : AdditionalAccountInformationType.DEDICATED_ACCOUNTS)
                   .orElse(AdditionalAccountInformationType.NONE);
    }

    public abstract List<AccountReference> getReferencesByType(List<AccountReference> accountReferences);
}
