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

package de.adorsys.psd2.core.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class Consent<T> {
    private T consentData;
    private String id;
    @Nullable
    private String internalRequestId;
    private ConsentStatus consentStatus;
    @NotNull
    private Integer frequencyPerDay;
    private boolean recurringIndicator;
    private boolean multilevelScaRequired;
    @Nullable
    private LocalDate validUntil;
    @Nullable
    private LocalDate expireDate;
    @Nullable
    private LocalDate lastActionDate;
    @Nullable
    private OffsetDateTime creationTimestamp;
    @Nullable
    private OffsetDateTime statusChangeTimestamp;
    @NotNull
    private ConsentTppInformation consentTppInformation;
    @NotNull
    private AuthorisationTemplate authorisationTemplate;
    @NotNull
    private List<PsuIdData> psuIdDataList;
    @NotNull
    private List<ConsentAuthorization> authorisations;
    @NotNull
    private Map<String, Integer> usages;
    @NotNull
    private AccountAccess tppAccountAccesses = AccountAccess.EMPTY_ACCESS;
    @NotNull
    private AccountAccess aspspAccountAccesses = AccountAccess.EMPTY_ACCESS;
    @Nullable
    private String instanceId;
    @NotNull
    private ConsentType consentType;
    private boolean signingBasketBlocked;
    private boolean signingBasketAuthorised;

    public TppInfo getTppInfo() {
        return Optional.ofNullable(consentTppInformation)
                   .map(ConsentTppInformation::getTppInfo)
                   .orElse(null);
    }

    public Optional<ConsentAuthorization> findAuthorisationInConsent(String authorisationId) {
        return getAuthorisations().stream()
                   .filter(auth -> auth.getId().equals(authorisationId))
                   .findFirst();
    }

    @JsonIgnore
    public boolean isExpired() {
        return getConsentStatus() == ConsentStatus.EXPIRED;
    }
}
