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

package de.adorsys.psd2.consent.api.ais;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsConsent {
    private String id;
    private byte[] consentData;
    private byte[] checksum;
    private ConsentStatus consentStatus;
    private ConsentType consentType;
    private ConsentTppInformation tppInformation;
    private AuthorisationTemplate authorisationTemplate;
    private String internalRequestId;
    private Integer frequencyPerDay;
    private LocalDate validUntil;
    private List<PsuIdData> psuIdDataList;
    private boolean recurringIndicator;
    private boolean multilevelScaRequired;
    private LocalDate lastActionDate;
    private LocalDate expireDate;
    private OffsetDateTime creationTimestamp;
    private OffsetDateTime statusChangeTimestamp;
    private List<Authorisation> authorisations;
    private Map<String, Integer> usages;
    private AccountAccess tppAccountAccesses;
    private AccountAccess aspspAccountAccesses;
    private String instanceId;
    private boolean signingBasketBlocked;
    private boolean signingBasketAuthorised;
}
