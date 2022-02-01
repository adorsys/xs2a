/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.aspsp.api.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Optional;

public interface CmsAspspTppService {

    /**
     * Loads TPP stop list record by TPP ID
     *
     * @param tppAuthorisationNumber ID of TPP to load
     * @param instanceId             optional ID of particular service instance
     * @return TPP Stop list object object if found in DB
     */
    @NotNull
    Optional<TppStopListRecord> getTppStopListRecord(@NotNull String tppAuthorisationNumber, @NotNull String instanceId);

    /**
     * Blocks requests from TPP by given TPP ID.
     * If lockPeriod parameter is passed, locks TPP for certain time.
     * If lockPeriod is not passed, lock TPP without time limitation.
     * If Record with given ID doesn't exist in DB, creates an empty one with given ID
     *
     * @param tppAuthorisationNumber ID of TPP to lock
     * @param instanceId             optional ID of particular service instance
     * @param lockPeriod             Time period of locking. May be omitted.
     * @return <code>true</code> if lock was done. <code>false</code> otherwise.
     */
    boolean blockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId, @Nullable Duration lockPeriod);

    /**
     * Releases lock of requests from TPP by given TPP ID.
     * If Record with given ID doesn't exist in DB, does nothing.
     *
     * @param tppAuthorisationNumber ID of TPP to lock
     * @param instanceId             optional ID of particular service instance
     * @return <code>true</code> if TPP was found and unlock was done. <code>false</code> otherwise.
     */
    boolean unblockTpp(@NotNull String tppAuthorisationNumber, @NotNull String instanceId);

    /**
     * Loads TPP info record by TPP authorisation number
     *
     * @param tppAuthorisationNumber ID of TPP to load
     * @param instanceId             optional ID of particular service instance
     * @return TPP info object if found in DB
     */
    @NotNull
    Optional<TppInfo> getTppInfo(@NotNull String tppAuthorisationNumber, @NotNull String instanceId);
}
