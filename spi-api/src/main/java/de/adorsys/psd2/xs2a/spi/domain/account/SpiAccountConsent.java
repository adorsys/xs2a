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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
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
public class SpiAccountConsent {
    // TODO remove ID and add externalId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/430
    private String id;
    private SpiAccountAccess access;
    private boolean recurringIndicator;
    private LocalDate validUntil;
    private int frequencyPerDay;
    private LocalDate lastActionDate;
    private ConsentStatus consentStatus;
    private boolean withBalance;
    private boolean tppRedirectPreferred;
    private List<SpiPsuData> psuData;
    private TppInfo tppInfo;
    private AisConsentRequestType aisConsentRequestType;
    private OffsetDateTime statusChangeTimestamp;
}
