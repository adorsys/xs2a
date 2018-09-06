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

package de.adorsys.aspsp.xs2a.spi.domain.consent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum SpiConsentAuthorizationResponseLinkType {
    START_AUTHORISATION_WITH_PSU_IDENTIFICATION("startAuthorisationWithPsuIdentification"),
    START_AUTHORISATION_WITH_PSU_AUTHENTICATION("startAuthorisationWithPsuAuthentication"),
    START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION("startAuthorisationWithAuthentication"),
    START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION("startAuthorisationWithTransactionAuthorisation");

    private static final Map<String, SpiConsentAuthorizationResponseLinkType> container = new HashMap<>();

    private String value;

    SpiConsentAuthorizationResponseLinkType(String value) {
        this.value = value;
    }

    static {
        for (SpiConsentAuthorizationResponseLinkType type : values()) {
            container.put(type.getValue(), type);
        }
    }

    public String getValue() {
        return value;
    }

    public static Optional<SpiConsentAuthorizationResponseLinkType> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}

