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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
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
    protected final Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;

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
        CreateAuthorisationRequest authorisationRequest = createAuthorisationRequest(createAuthorisationRequest.getAuthorisationId(),
                                                                                     createAuthorisationRequest.getScaStatus(),
                                                                                     psuData,
                                                                                     createAuthorisationRequest.getScaApproach());
        return consentService.createConsentAuthorisation(consentId, authorisationRequest)
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

    protected CreateAuthorisationRequest createAuthorisationRequest(String authorisationId, ScaStatus scaStatus, PsuIdData psuData,
                                                                    ScaApproach scaApproach) {
        return xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(authorisationId,
                                                                        scaStatus,
                                                                        psuData,
                                                                        scaApproach);
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
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return consentService.getAuthorisationScaStatus(consentId, authorisationId);
    }
}
