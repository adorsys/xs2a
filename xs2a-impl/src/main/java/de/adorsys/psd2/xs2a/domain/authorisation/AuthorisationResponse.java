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

package de.adorsys.psd2.xs2a.domain.authorisation;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;

import java.util.Set;

/**
 * Common authorisation response to be used in AIS and PIS on creating or updating the authorisation
 */
public interface AuthorisationResponse {
    String getAuthorisationId();
    void setPsuMessage(String psuMessage);
    default Set<TppMessageInformation> getTppMessageInformation() {
        return null;
    }
}
