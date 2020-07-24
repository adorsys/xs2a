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

package de.adorsys.psd2.consent.api.piis.v2;

import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CmsConfirmationOfFundsConsent {
    private String id;
    private LocalDate validUntil;
    private LocalDate expireDate;
    private LocalDate lastActionDate;
    private ConsentStatus consentStatus;
    private boolean tppRedirectPreferred;
    private List<PsuIdData> psuIdDataList;
    private TppInfo tppInfo;
    private AuthorisationTemplate authorisationTemplate;
    private boolean multilevelScaRequired;
    private OffsetDateTime creationTimestamp;
    private OffsetDateTime statusChangeTimestamp;
    private List<CmsConfirmationOfFundsAuthorisation> authorisations;
}
