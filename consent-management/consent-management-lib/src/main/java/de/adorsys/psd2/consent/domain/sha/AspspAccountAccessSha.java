/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.sha;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class AspspAccountAccessSha extends AccountAccessSha {
    private String resourceId;
    private String aspspAccountId;

    public AspspAccountAccessSha(String accountIdentifier, String currency, String typeAccess, String accountReferenceType, String resourceId, String aspspAccountId) {
        super(accountIdentifier, currency, typeAccess, accountReferenceType);
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
    }

    public boolean isNotEmpty() {
        return StringUtils.isNoneBlank(resourceId, aspspAccountId);
    }
}
