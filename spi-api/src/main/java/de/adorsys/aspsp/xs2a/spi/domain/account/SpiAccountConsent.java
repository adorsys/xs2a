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

package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import lombok.Data;

import java.util.Date;

@Data
public class SpiAccountConsent {
    private final String id;
    private final SpiAccountAccess access;
    private final boolean recurringIndicator;
    private final Date validUntil;
    private final int frequencyPerDay;
    private final Date lastActionDate;
    private final SpiTransactionStatus spiTransactionStatus;
    private final SpiConsentStatus spiConsentStatus;
    private final boolean withBalance;
    private final boolean tppRedirectPreferred;
}
