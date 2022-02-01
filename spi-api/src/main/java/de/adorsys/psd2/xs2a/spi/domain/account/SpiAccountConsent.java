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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.spi.domain.SpiConsent;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SpiAccountConsent implements SpiConsent {
    private String id;
    private SpiAccountAccess access;
    private boolean recurringIndicator;
    private LocalDate validUntil;
    private LocalDate expireDate;
    private int frequencyPerDay;
    private LocalDate lastActionDate;
    private ConsentStatus consentStatus;
    private boolean withBalance;
    private boolean tppRedirectPreferred;
    private List<SpiPsuData> psuData;
    private TppInfo tppInfo;
    private AisConsentRequestType aisConsentRequestType;
    private OffsetDateTime statusChangeTimestamp;
    private OffsetDateTime creationTimestamp;
    private String instanceId;
    private ConsentType consentType;
}
