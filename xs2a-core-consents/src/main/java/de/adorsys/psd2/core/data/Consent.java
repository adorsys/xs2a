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
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@SuperBuilder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public abstract class Consent<T> implements ConsentAutorizable {
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
        return Optional.of(consentTppInformation)
                   .map(ConsentTppInformation::getTppInfo)
                   .orElse(null);
    }

    @Override
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
