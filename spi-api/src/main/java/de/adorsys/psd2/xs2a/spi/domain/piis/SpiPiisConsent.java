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

package de.adorsys.psd2.xs2a.spi.domain.piis;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.spi.domain.SpiConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpiPiisConsent implements SpiConsent {
    private String id;
    private boolean recurringIndicator;
    private OffsetDateTime requestDateTime;
    private LocalDate lastActionDate;
    private LocalDate expireDate;
    private List<SpiPsuData> psuData;
    private ConsentStatus consentStatus;
    private SpiAccountReference account;
    private OffsetDateTime creationTimestamp;
    private String instanceId;
    private String cardNumber;
    private LocalDate cardExpiryDate;
    private String cardInformation;
    private String registrationInformation;
    private OffsetDateTime statusChangeTimestamp;
    private String tppAuthorisationNumber;
    private ConsentType consentType;
}
