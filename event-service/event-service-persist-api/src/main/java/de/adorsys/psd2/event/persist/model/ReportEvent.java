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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class ReportEvent {
    private Long id;
    private OffsetDateTime timestamp;
    private String consentId;
    private String paymentId;
    private byte[] payload;
    private EventOrigin eventOrigin;
    private EventType eventType;
    private String instanceId;
    private String tppAuthorisationNumber;
    private String xRequestId;
    private String internalRequestId;
    private Set<PsuIdDataPO> psuIdData = new HashSet<>();

    public ReportEvent merge(ReportEvent other) {
        this.psuIdData.addAll(other.psuIdData);
        return this;
    }
}
