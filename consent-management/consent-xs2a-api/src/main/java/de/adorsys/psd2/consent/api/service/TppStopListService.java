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

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;

public interface TppStopListService {

    /**
     * Checks if TPP is blocked.
     *
     * @param tppInfo information about particular TPP from TPP Certificate
     * @return <code>true</code> if TPP is found and has status BLOCKED, <code>false</code> if TPP is not found or its status is not BLOCKED
     */
    boolean checkIfTppBlocked(TppInfo tppInfo);
}
