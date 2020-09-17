/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
