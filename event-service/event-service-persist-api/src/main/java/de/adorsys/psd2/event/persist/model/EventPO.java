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

package de.adorsys.psd2.event.persist.model;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
public class EventPO {
    private Long id;
    private OffsetDateTime timestamp;
    @Nullable
    private String consentId;
    @Nullable
    private String paymentId;
    @Nullable
    private byte[] payload;
    private EventOrigin eventOrigin;
    private EventType eventType;
    private String instanceId;
    private String tppAuthorisationNumber;
    private String xRequestId;
    @Nullable
    private PsuIdDataPO psuIdData;
    private String internalRequestId;
}
