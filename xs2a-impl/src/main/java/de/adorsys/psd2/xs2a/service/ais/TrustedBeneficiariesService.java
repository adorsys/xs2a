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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aTrustedBeneficiariesMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTrustedBeneficiariesListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetTrustedBeneficiariesListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;

@Slf4j
@Service
@AllArgsConstructor
public class TrustedBeneficiariesService {
    private final AccountSpi accountSpi;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetTrustedBeneficiariesListValidator getTrustedBeneficiariesListValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;
    private final LoggingContextService loggingContextService;
    private final SpiToXs2aTrustedBeneficiariesMapper spiToXs2aTrustedBeneficiariesMapper;

    /**
     * Gets TrustedBeneficiaries based on accountId, beneficiaries get checked with provided AIS-consent
     *
     * @param consentId  String representing an Consent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param requestUri the URI of incoming request
     * @return response with {@link Xs2aTrustedBeneficiariesList} based on accountId and granted by consent
     */
    public ResponseObject<Xs2aTrustedBeneficiariesList> getTrustedBeneficiaries(String consentId,
                                                                                String accountId,
                                                                                String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_TRUSTED_BENEFICIARIES_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            return getResponseWithNotFoundConsent(consentId, accountId);
        }

        AisConsent aisConsent = aisConsentOptional.get();
        ValidationResult validationResult = getValidationResult(accountId, requestUri, aisConsent);

        if (validationResult.isNotValid()) {
            return getResponseWithValidationError(consentId, accountId, requestUri, validationResult);
        }

        SpiResponse<List<SpiTrustedBeneficiaries>> spiResponse = getSpiResponse(aisConsent, consentId, accountId);

        if (spiResponse.hasError()) {
            return getResponseWithSpiError(consentId, spiResponse);
        }

        return getSuccessfulResponse(consentId, accountId, requestUri, aisConsent, spiResponse);
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getResponseWithNotFoundConsent(String consentId, String accountId) {
        log.info("Account-ID [{}], Consent-ID [{}]. Get trusted beneficiaries list failed. Account consent not found by id",
                 accountId, consentId);
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                   .build();
    }

    private ValidationResult getValidationResult(String accountId, String requestUri, AisConsent aisConsent) {
        GetTrustedBeneficiariesListConsentObject validatorObject = new GetTrustedBeneficiariesListConsentObject(aisConsent, accountId, requestUri);
        return getTrustedBeneficiariesListValidator.validate(validatorObject);
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getResponseWithValidationError(String consentId, String accountId, String requestUri, ValidationResult validationResult) {
        log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get trusted beneficiaries list - validation failed: {}",
                 accountId, consentId, requestUri, validationResult.getMessageError());
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(validationResult.getMessageError())
                   .build();
    }

    private SpiResponse<List<SpiTrustedBeneficiaries>> getSpiResponse(AisConsent aisConsent, String consentId, String accountId) {
        return accountSpi.requestTrustedBeneficiariesList(accountHelperService.getSpiContextData(),
                                                          accountHelperService.findAccountReference(aisConsent.getAspspAccountAccesses().getAccounts(), accountId),
                                                          consentMapper.mapToSpiAccountConsent(aisConsent),
                                                          aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getResponseWithSpiError(String consentId, SpiResponse<List<SpiTrustedBeneficiaries>> spiResponse) {
        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("Consent-ID: [{}]. Get trusted beneficiaries list failed: couldn't get trusted beneficiaries. Error msg: [{}]",
                 consentId, errorHolder);
        return ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                   .fail(new MessageError(errorHolder))
                   .build();
    }

    private ResponseObject<Xs2aTrustedBeneficiariesList> getSuccessfulResponse(String consentId, String accountId, String requestUri, AisConsent aisConsent, SpiResponse<List<SpiTrustedBeneficiaries>> spiResponse) {
        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        List<Xs2aTrustedBeneficiaries> trustedBeneficiaries = spiToXs2aTrustedBeneficiariesMapper.mapToXs2aTrustedBeneficiariesList(spiResponse.getPayload());
        Xs2aTrustedBeneficiariesList xs2aTrustedBeneficiariesList = new Xs2aTrustedBeneficiariesList(trustedBeneficiaries);

        ResponseObject<Xs2aTrustedBeneficiariesList> response = ResponseObject.<Xs2aTrustedBeneficiariesList>builder()
                                                                    .body(xs2aTrustedBeneficiariesList)
                                                                    .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.BENEFICIARIES, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(aisConsent),
                                           accountId, null);
        return response;
    }
}
