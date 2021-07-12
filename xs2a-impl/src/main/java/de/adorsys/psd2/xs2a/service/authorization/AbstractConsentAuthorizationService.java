/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public abstract class AbstractConsentAuthorizationService<T> implements ConsentAuthorizationService {
    private final Xs2aConsentService consentService;
    private final Xs2aAuthorisationService authorisationService;
    private final ConsentPsuDataMapper consentPsuDataMapper;

    protected abstract Optional<T> getConsentById(String consentId);

    protected abstract void updateConsentAuthorisation(ConsentAuthorisationsParameters mapToUpdateConsentPsuDataReq);

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(@NotNull Xs2aCreateAuthorisationRequest createAuthorisationRequest) {
        String consentId = createAuthorisationRequest.getConsentId();
        Optional<T> consentOptional = getConsentById(consentId);
        if (consentOptional.isEmpty()) {
            log.info("Consent-ID [{}]. Create consent authorisation has failed. Consent not found by id.", consentId);
            return Optional.empty();
        }

        PsuIdData psuData = createAuthorisationRequest.getPsuData();
        return consentService.createConsentAuthorisation(consentId,
                                                         createAuthorisationRequest.getAuthorisationId(),
                                                         createAuthorisationRequest.getScaApproach(),
                                                         createAuthorisationRequest.getScaStatus(),
                                                         psuData)
                   .map(auth -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorisationId(auth.getAuthorizationId());
                       resp.setScaStatus(auth.getScaStatus());
                       resp.setPsuIdData(psuData);
                       resp.setInternalRequestId(auth.getInternalRequestId());
                       resp.setScaApproach(auth.getScaApproach());
                       return resp;
                   });
    }

    @Override
    public AuthorisationProcessorResponse updateConsentPsuData(CommonAuthorisationParameters request, AuthorisationProcessorResponse response) {
        if (response.hasError()) {
            log.info("Consent-ID [{}], Authentication-ID [{}], PSU-ID [{}]. Update consent authorisation has failed. Error msg: {}.",
                request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData().getPsuId(), response.getErrorHolder());
        } else {
            updateConsentAuthorisation(consentPsuDataMapper.mapToUpdateConsentPsuDataReq(request, response));
        }
        return response;
    }

    /**
     * Gets ConsentAuthorization using provided authorisation id and consent id by invoking CMS through ConsentService.
     * See {@link Xs2aAuthorisationService#getAuthorisationById(String)} (String)} for details
     *
     * @param authorisationId String identification of ConsentAuthorization
     * @return ConsentAuthorization instance
     */
    @Override
    public Optional<Authorisation> getConsentAuthorizationById(String authorisationId) {
        return authorisationService.getAuthorisationById(authorisationId);
    }

    /**
     * Gets SCA status of the authorisation from CMS
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId){
        return consentService.getAuthorisationScaStatus(consentId, authorisationId);
    }
}
