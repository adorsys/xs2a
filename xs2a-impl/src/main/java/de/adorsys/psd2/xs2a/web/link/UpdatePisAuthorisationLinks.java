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

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.aspect.UrlHolder;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.EMBEDDED;

public class UpdatePisAuthorisationLinks extends AbstractLinks {

    public UpdatePisAuthorisationLinks(String httpUrl, ScaApproachResolver scaApproachResolver,
                                       Xs2aUpdatePisCommonPaymentPsuDataResponse response,
                                       Xs2aCreatePisAuthorisationRequest createRequest) {
        super(httpUrl);

        ScaStatus scaStatus = response.getScaStatus();

        HrefType authorisationLink = buildAuthorisationLink(response, createRequest);
        setScaStatus(authorisationLink);

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            setSelectAuthenticationMethod(authorisationLink);
        } else if (isScaStatusMethodSelected(response.getChosenScaMethod(), scaStatus) &&
                       scaApproachResolver.getInitiationScaApproach(response.getAuthorisationId()) == EMBEDDED) {
            setAuthoriseTransaction(authorisationLink);
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            setUpdatePsuAuthentication(authorisationLink);
        }
    }

    private HrefType buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataResponse response, Xs2aCreatePisAuthorisationRequest createRequest) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, createRequest.getPaymentService().getValue(),
                         createRequest.getPaymentProduct(), createRequest.getPaymentId(), response.getAuthorisationId());
    }
}
