/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;

public abstract class UpdateConsentLinks extends AbstractLinks {

    public UpdateConsentLinks(String httpUrl, ScaApproachResolver scaApproachResolver, UpdateConsentPsuDataResponse response) {
        super(httpUrl);

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorisationId();
        ScaStatus scaStatus = response.getScaStatus();

        HrefType authorisationLink = buildPath(getPath(), consentId, authorisationId);
        setScaStatus(authorisationLink);

        if (scaStatus == ScaStatus.PSUAUTHENTICATED) {
            setSelectAuthenticationMethod(authorisationLink);
        } else if (scaStatus == ScaStatus.SCAMETHODSELECTED) {
            ScaApproach scaApproach = scaApproachResolver.getScaApproach(authorisationId);
            if (scaApproach != ScaApproach.DECOUPLED) {
                setAuthoriseTransaction(authorisationLink);
            }
        } else if (scaStatus == ScaStatus.PSUIDENTIFIED) {
            setUpdatePsuAuthentication(authorisationLink);
        }
    }

    abstract String getPath();
}
