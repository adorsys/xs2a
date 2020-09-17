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
}
