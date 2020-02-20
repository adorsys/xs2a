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
            return null;
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
