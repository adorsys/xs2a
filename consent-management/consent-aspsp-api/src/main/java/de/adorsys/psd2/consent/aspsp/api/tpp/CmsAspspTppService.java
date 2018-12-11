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

package de.adorsys.psd2.consent.aspsp.api.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public interface CmsAspspTppService {

    /**
     * Loads TPP black list record by TPP ID and National competent authority ID
     *
     * @param tppAuthorisationNumber ID of TPP to load
     * @param nationalAuthorityId    National competent authority id
     * @return TPP Stop list object object if found in DB
     */
    @NotNull
    Optional<TppStopListRecord> getTppBlackListRecord(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId);

    /**
     * Blocks requests from TPP by given TPP ID and National competent authority ID.
     * If lockPeriod parameter is passed, locks TPP for certain time.
     * If lockPeriod is not passed, lock TPP without time limitation.
     * If Record with given ID doesn't exist in DB, creates an empty one with given ID
     *
     * @param tppAuthorisationNumber ID of TPP to lock
     * @param nationalAuthorityId    National competent authority id
     * @param lockPeriod             Time period of locking. May be omitted.
     * @return <code>true</code> if lock was done. <code>false</code> otherwise.
     */
    boolean blockTpp(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId, @Nullable Duration lockPeriod);

    /**
     * Releases lock of requests from TPP by given TPP ID and National competent authority ID.
     * If Record with given ID doesn't exist in DB, does nothing.
     *
     * @param tppAuthorisationNumber ID of TPP to lock
     * @param nationalAuthorityId    National competent authority id
     * @return <code>true</code> if TPP was found and unlock was done. <code>false</code> otherwise.
     */
    boolean unblockTpp(@NotNull String tppAuthorisationNumber, @NotNull String nationalAuthorityId);
}
