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

package de.adorsys.psd2.xs2a.core.piis;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PiisConsent {
    private String id;
    private boolean recurringIndicator;
    private OffsetDateTime requestDateTime;
    private LocalDate lastActionDate;
    private LocalDate expireDate;
    private PsuIdData psuData;
    private TppInfo tppInfo;
    private ConsentStatus consentStatus;
    private AccountReference account;
    private PiisConsentTppAccessType tppAccessType;
    private int allowedFrequencyPerDay;
    private OffsetDateTime creationTimestamp;
    private String instanceId;
    private String cardNumber;
    private LocalDate cardExpiryDate;
    private String cardInformation;
    private String registrationInformation;
    private OffsetDateTime statusChangeTimestamp;

    /**
     * @return Account Reference list
     *
     * @deprecated since 2.4 and will be removed in 2.7, use getAccount instead
     */
    @Deprecated //TODO 2.7 Remove this method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/805
    public List<AccountReference> getAccounts() {
        return Collections.singletonList(account);
    }

    /**
     *
     * @param accounts Account Reference list
     *
     * @deprecated since 2.4 and will be removed in 2.7, use setAccount instead
     */
    @Deprecated //TODO 2.7 Remove this method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/805
    public void setAccounts(List<AccountReference> accounts) {
        if (CollectionUtils.isEmpty(accounts)) {
            account = null;
        }

        account = accounts.get(0);
    }
}
