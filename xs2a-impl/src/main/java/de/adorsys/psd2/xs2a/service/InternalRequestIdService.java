/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.InternalRequestIdHolder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalRequestIdService {
    private final InternalRequestIdHolder internalRequestIdHolder;

    /**
     * Returns unique internal request ID that was assigned to the current request
     *
     * @return internal request ID
     */
    @NotNull
    public UUID getInternalRequestId() {
        UUID idFromHolder = internalRequestIdHolder.getInternalRequestId();

        if (idFromHolder == null) {
            return generateInternalRequestId();
        }

        return idFromHolder;
    }

    @NotNull
    private UUID generateInternalRequestId() {
        UUID internalRequestId = UUID.randomUUID();
        internalRequestIdHolder.setInternalRequestId(internalRequestId);
        return internalRequestId;
    }
}
