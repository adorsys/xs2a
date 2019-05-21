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

package de.adorsys.psd2.consent.service.security;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DecryptedIdSet {
    private String decryptedCompositeId;
    private String decryptedId;
    private String randomSecretKey;
    private String dataEncryptionProviderId;

    public DecryptedIdSet(String[] idDataValues, String dataEncryptionProviderId) {
        if (idDataValues.length > 0) {
            this.decryptedId = idDataValues[0];
        }

        if (idDataValues.length > 1) {
            this.randomSecretKey = idDataValues[1];
        }

        if (idDataValues.length > 2) {
            this.dataEncryptionProviderId = idDataValues[2];
        } else {
            this.dataEncryptionProviderId = dataEncryptionProviderId;
        }
    }
}
