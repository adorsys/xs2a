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

package de.adorsys.psd2.consent.api.service;

import java.util.Optional;

/**
 * PisConsentService with enabled encryption and decryption
 *
 * @see de.adorsys.psd2.consent.api.service.PisConsentServiceBase
 * @see de.adorsys.psd2.consent.api.service.PisConsentService
 */
public interface PisConsentServiceEncrypted extends PisConsentServiceBase {
    /**
     * Gets original decrypted Id from encrypted string
     *
     * @param encryptedId id to be decrypted
     * @return Response containing original decrypted Id
     */
    Optional<String> getDecryptedId(String encryptedId);
}
