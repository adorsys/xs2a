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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.util.Currency;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
@ApiModel(description = "Account access", value = "AccountAccess")
public class TppAccountAccess extends AccountAccess {

    public TppAccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency) {
        super(accountIdentifier, typeAccess, accountReferenceType, currency);
    }
}
