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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPiisConsentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aPiisConsentService {
    private final ConsentServiceEncrypted consentService;
    private final Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    private final AisConsentServiceEncrypted aisConsentService;
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aConsentAuthorisationMapper consentAuthorisationMapper;
    private final LoggingContextService loggingContextService;

    public Optional<Xs2aCreatePiisConsentResponse> createConsent(CreatePiisConsentRequest request, PsuIdData psuData, TppInfo tppInfo) {
        CmsConsent cmsConsent = xs2aPiisConsentMapper.mapToCmsConsent(request, psuData, tppInfo);

        CmsResponse<CmsCreateConsentResponse> response;
        try {
            response = consentService.createConsent(cmsConsent);
        } catch (WrongChecksumException e) {
            log.info("Consent cannot be created, checksum verification failed");
            return Optional.empty();
        }

        if (response.hasError()) {
            log.info("Consent cannot be created, because can't save to cms DB");
            return Optional.empty();
        }

        CmsCreateConsentResponse createConsentResponse = response.getPayload();

        PiisConsent piisConsent = xs2aPiisConsentMapper.mapToPiisConsent(createConsentResponse.getCmsConsent());

        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = new Xs2aCreatePiisConsentResponse(createConsentResponse.getConsentId(), piisConsent);
        return Optional.of(xs2aCreatePiisConsentResponse);
    }

    public Optional<PiisConsent> getPiisConsentById(String consentId) {
        CmsResponse<CmsConsent> consentById = consentService.getConsentById(consentId);

        if (consentById.hasError()) {
            log.info("Get consent by id failed due to CMS problems");
            return Optional.empty();
        }

        PiisConsent piisConsent = xs2aPiisConsentMapper.mapToPiisConsent(consentById.getPayload());
        return Optional.ofNullable(piisConsent);
    }

    public void updateConsentStatus(String consentId, ConsentStatus consentStatus) {
        CmsResponse<Boolean> statusUpdated;
        try {
            statusUpdated = consentService.updateConsentStatusById(consentId, consentStatus);
        } catch (WrongChecksumException e) {
            log.info("updateConsentStatus cannot be executed, checksum verification failed");
            return;
        }

        if (statusUpdated.isSuccessful() && BooleanUtils.isTrue(statusUpdated.getPayload())) {
            loggingContextService.storeConsentStatus(consentStatus);
        }
    }

    public CmsResponse<PiisConsent> updateAspspAccountAccess(String consentId, AccountAccess accountAccess) {
        CmsResponse<CmsConsent> response;

        CmsResponse.CmsResponseBuilder<PiisConsent> builder = CmsResponse.builder();

        try {
            response = aisConsentService.updateAspspAccountAccess(consentId, accountAccess);
        } catch (WrongChecksumException e) {
            return builder.error(CmsError.CHECKSUM_ERROR).build();
        }

        if (response.hasError()) {
            return builder.error(response.getError()).build();
        }

        return builder.payload(xs2aPiisConsentMapper.mapToPiisConsent(response.getPayload())).build();
    }

    public boolean findAndTerminateOldConsentsByNewConsentId(String newConsentId) {
        CmsResponse<Boolean> response = consentService.findAndTerminateOldConsentsByNewConsentId(newConsentId);
        return response.isSuccessful() && response.getPayload();
    }

    public void updateConsentAuthorisation(UpdateConsentPsuDataReq updatePsuData) {
        Optional.ofNullable(updatePsuData)
            .ifPresent(req -> {
                final UpdateAuthorisationRequest request = consentAuthorisationMapper.mapToAuthorisationRequest(req);

                authorisationService.updateAuthorisation(request, req.getAuthorizationId());
            });
    }

    /**
     * Updates multilevel SCA required field
     *
     * @param consentId             String representation of the consent identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     */
    public void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired) {
        try {
            consentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
        } catch (WrongChecksumException e) {
            log.info("updateMultilevelScaRequired cannot be executed, checksum verification failed");
        }
    }
}
