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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.PiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.ais.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentAuthorisationObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_403;

@Slf4j
@Service
@RequiredArgsConstructor
public class PiisConsentAuthorisationService {
    private final Xs2aEventService xs2aEventService;
    private final Xs2aPiisConsentService xs2aPiisConsentService;
    private final PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;
    private final ConfirmationOfFundsConsentValidationService confirmationOfFundsConsentValidationService;

    public ResponseObject<AuthorisationResponse> createPiisAuthorisation(PsuIdData psuData, String consentId, String password) {
        ResponseObject<CreateConsentAuthorizationResponse> createAisAuthorizationResponse = createConsentAuthorizationWithResponse(psuData, consentId);

        if (createAisAuthorizationResponse.hasError()) {
            return ResponseObject.<AuthorisationResponse>builder()
                       .fail(createAisAuthorizationResponse.getError())
                       .build();
        }

        return ResponseObject.<AuthorisationResponse>builder()
                   .body(createAisAuthorizationResponse.getBody())
                   .build();
    }

    private ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuDataFromRequest, String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.START_PIIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        Optional<PiisConsent> piisConsentOptional = xs2aPiisConsentService.getPiisConsentById(consentId);

        if (piisConsentOptional.isEmpty()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response failed: consent not found by id",
                     consentId);
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(PIIS_403, of(CONSENT_UNKNOWN_403)).build();
        }
        PiisConsent piisConsent = piisConsentOptional.get();

        ValidationResult validationResult = confirmationOfFundsConsentValidationService.validateConsentAuthorisationOnCreate(new CreatePiisConsentAuthorisationObject(piisConsent, psuDataFromRequest));

        if (validationResult.isNotValid()) {
            log.info("Consent-ID: [{}]. Create consent authorisation with response - validation failed: {}",
                     consentId, validationResult.getMessageError());
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        PiisAuthorizationService service = piisScaAuthorisationServiceResolver.getService();
        PsuIdData psuIdData = getActualPsuData(psuDataFromRequest, piisConsent);

        return service.createConsentAuthorization(psuIdData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder()
                                  .fail(PIIS_403, of(CONSENT_UNKNOWN_403))
                                  ::build);
    }

    private PsuIdData getActualPsuData(PsuIdData psuDataFromRequest, PiisConsent piisConsent) {
        boolean isMultilevel = piisConsent.isMultilevelScaRequired();

        if (psuDataFromRequest.isNotEmpty() || isMultilevel) {
            return psuDataFromRequest;
        }

        return piisConsent.getPsuIdDataList().stream()
                   .findFirst()
                   .orElse(psuDataFromRequest);
    }
}
