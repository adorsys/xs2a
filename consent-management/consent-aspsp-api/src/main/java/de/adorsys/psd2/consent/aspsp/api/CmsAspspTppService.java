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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public interface CmsAspspTppService {
    /**
     * Loads TPP Information by its ID
     * @param tppAuthorizationNumber ID of TPP to load
     * @return TPP Info object if found in DB
     */
    @NotNull
    Optional<TppInfo> getTppInfoById(@NotNull String tppAuthorizationNumber);

    /**
     * Blocks requests from TPP by given TPP ID. If lockPeriod parameter is passed, locks TPP for certain time.
     * If lockPeriod is not passed, lock TPP without time limitation.
     * If TPP with given ID doesn't exist in DB, creates an empty one with given ID
     *
     * @param tppAuthorizationNumber ID of TPP to lock
     * @param lockPeriod Time period of locking. May be omitted.
     * @return <code>true</code> if lock was done. <code>false</code> otherwise.
     */
    boolean blockTpp(@NotNull String tppAuthorizationNumber, @Nullable Duration lockPeriod);

    /**
     * Releases lock of requests from TPP by given TPP ID.
     * If TPP with given ID doesn't exist in DB, does nothing.
     *
     * @param tppAuthorizationNumber ID of TPP to lock
     * @return <code>true</code> if TPP was found and unlock was done. <code>false</code> otherwise.
     */
    boolean unblockTpp(@NotNull String tppAuthorizationNumber);
}
