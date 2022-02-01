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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreateAisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.profile.FrequencyPerDateCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aAbstractConsentService {
    private final ConsentServiceEncrypted consentService;
    private final AisConsentServiceEncrypted aisConsentService;
    private final Xs2aAuthorisationService authorisationService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aConsentAuthorisationMapper consentAuthorisationMapper;
    private final FrequencyPerDateCalculationService frequencyPerDateCalculationService;
    private final LoggingContextService loggingContextService;

    /**
     * Sends a POST request to CMS to store created AIS consent
     *
     * @param request Request body storing main consent details
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param tppInfo Information about particular TPP from TPP Certificate
     * @return create consent response, containing consent and its encrypted ID
     */
    public Optional<Xs2aCreateAisConsentResponse> createConsent(CreateConsentReq request, PsuIdData psuData, TppInfo tppInfo) {
        int allowedFrequencyPerDay = frequencyPerDateCalculationService.getMinFrequencyPerDay(request.getFrequencyPerDay());
        CmsConsent cmsConsent = aisConsentMapper.mapToCmsConsent(request, psuData, tppInfo, allowedFrequencyPerDay);

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
        return Optional.of(new Xs2aCreateAisConsentResponse(createConsentResponse.getConsentId(),
                                                            aisConsentMapper.mapToAisConsent(createConsentResponse.getCmsConsent()),
                                                            createConsentResponse.getCmsConsent().getTppInformation().getTppNotificationSupportedModes()));
    }
}
