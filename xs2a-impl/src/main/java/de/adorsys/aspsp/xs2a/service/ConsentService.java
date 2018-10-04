/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.aspsp.profile.domain.ScaApproach;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentStatus.RECEIVED;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final AisConsentService aisConsentService;
    private final AisAuthorizationService aisAuthorizationService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final PisScaAuthorisationService pisAuthorizationService;
    private final TppService tppService;
    private final AuthorisationMethodService authorisationMethodService;

    /**
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuId   String representing PSU identification at ASPSP
     * @return CreateConsentResponse representing the complete response to create consent request
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, String psuId, boolean explicitPreferred) {
        if (isNotSupportedGlobalConsentForAllPsd2(request)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
        }
        if (isNotSupportedBankOfferedConsent(request)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PARAMETER_NOT_SUPPORTED))).build();
        }
        if (!isValidExpirationDate(request.getValidUntil())) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.PERIOD_INVALID))).build();
        }

        if (isConsentGlobal(request) || isConsentForAllAvailableAccounts(request)) {
            request.setAccess(getAccessForGlobalOrAllAvailableAccountsConsent(request));
        }

        String tppId = tppService.getTppId();
        String consentId = aisConsentService.createConsent(request, psuId, tppId, new AspspConsentData());

        //TODO v1.2 Add embedded approach specfic links
        if (StringUtils.isBlank(consentId)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_400))).build();
        }

        ResponseObject<CreateConsentResponse> createConsentResponseObject = ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(RECEIVED.getValue(), consentId, null, null, null, null)).build();

        if (aspspProfileService.getScaApproach() == ScaApproach.EMBEDDED
                && authorisationMethodService.isImplicitMethod(explicitPreferred)) {
            proceedEmbeddedImplicitCaseForCreateConsent(createConsentResponseObject.getBody(), psuId, consentId);
        }

        return createConsentResponseObject;
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     * Returns status of requested consent
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        return Optional.ofNullable(getValidatedSpiAccountConsent(consentId))
                   .map(consent -> aisConsentMapper.mapToConsentStatus(consent.getConsentStatus()))
                   .map(status -> ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(status.get())).build())
                   .orElseGet(() -> ResponseObject.<ConsentStatusResponse>builder()
                                        .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400)))
                                        .build());
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     * Revokes account consent on PSU request
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        if (getValidatedSpiAccountConsent(consentId) != null) {
            aisConsentService.revokeConsent(consentId);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        AccountConsent consent = aisConsentMapper.mapToAccountConsent(getValidatedSpiAccountConsent(consentId));
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    ResponseObject<Xs2aAccountAccess> getValidatedConsent(String consentId) {
        SpiAccountConsent spiAccountConsent = getValidatedSpiAccountConsent(consentId);

        if (spiAccountConsent == null || LocalDate.now().compareTo(spiAccountConsent.getValidUntil()) >= 0) {
            return ResponseObject.<Xs2aAccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
        }

        AccountConsent consent = aisConsentMapper.mapToAccountConsent(spiAccountConsent);

        if (!consent.isValidStatus()) {
            return ResponseObject.<Xs2aAccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }
        if (!consent.isValidFrequency()) {
            return ResponseObject.<Xs2aAccountAccess>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.ACCESS_EXCEEDED))).build();
        }
        return ResponseObject.<Xs2aAccountAccess>builder().body(consent.getAccess()).build();
    }

    public ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(String psuId, String consentId) {
        return aisAuthorizationService.createConsentAuthorization(psuId, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(() -> ResponseObject.<CreateConsentAuthorizationResponse>builder().fail(new MessageError(MessageErrorCode.CONSENT_UNKNOWN_400)).build());
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(aisAuthorizationService.getAccountConsentAuthorizationById(updatePsuData.getAuthorizationId(), updatePsuData.getConsentId()))
                   .map(conAuth -> getUpdateConsentPsuDataResponse(updatePsuData, conAuth))
                   .orElseGet(() -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                        .build());
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = aisAuthorizationService.updateConsentPsuData(updatePsuData, consentAuthorization);

        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getErrorCode())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(new MessageError(e))
                                               .build())
                                 .orElseGet(() -> ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response).build()))
                   .orElseGet(() -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                        .build());
    }

    public ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> createPisConsentAuthorization(String paymentId, PaymentType paymentType) {
        return pisAuthorizationService.createConsentAuthorisation(paymentId, paymentType)
                   .map(resp -> ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(() -> ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                                        .build());
    }

    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentPsuData(UpdatePisConsentPsuDataRequest request) {
        return pisAuthorizationService.updateConsentPsuData(request)
                   .map(r -> ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                                 .body(r).build())
                   .orElseGet(() -> ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                                        .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                        .build());

    }


    boolean isValidAccountByAccess(String iban, Currency currency, List<Xs2aAccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getIban().equals(iban)
                                             && a.getCurrency() == currency);
    }

    private boolean isValidExpirationDate(LocalDate validUntil) {
        int consentLifetime = Math.abs(aspspProfileService.getConsentLifetime());
        return validUntil.isAfter(LocalDate.now()) && isValidConsentLifetime(consentLifetime, validUntil);
    }

    private boolean isValidConsentLifetime(int consentLifetime, LocalDate validUntil) {
        return consentLifetime == 0 || validUntil.isBefore(LocalDate.now().plusDays(consentLifetime));
    }

    private Boolean isNotEmptyAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(Xs2aAccountAccess::isNotEmpty)
                   .orElse(false);
    }

    private boolean isNotSupportedGlobalConsentForAllPsd2(CreateConsentReq request) {
        return isConsentGlobal(request)
                   && !aspspProfileService.getAllPsd2Support();
    }

    private boolean isNotSupportedBankOfferedConsent(CreateConsentReq request) {
        return !isNotEmptyAccess(request.getAccess())
                   && !aspspProfileService.isBankOfferedConsentSupported();
    }

    private boolean isConsentGlobal(CreateConsentReq request) {
        return isNotEmptyAccess(request.getAccess())
                   && request.getAccess().getAllPsd2() == ALL_ACCOUNTS;
    }

    private boolean isConsentForAllAvailableAccounts(CreateConsentReq request) {
        return request.getAccess().getAvailableAccounts() == ALL_ACCOUNTS
                   || request.getAccess().getAvailableAccounts() == ALL_ACCOUNTS_WITH_BALANCES;
    }

    private Xs2aAccountAccess getAccessForGlobalOrAllAvailableAccountsConsent(CreateConsentReq request) {
        return new Xs2aAccountAccess(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            request.getAccess().getAvailableAccounts(),
            request.getAccess().getAllPsd2()
        );
    }

    private SpiAccountConsent getValidatedSpiAccountConsent(String consentId) {
        return Optional.ofNullable(aisConsentService.getAccountConsentById(consentId))
                   .filter(consent -> tppService.getTppId().equals(consent.getTppId()))
                   .orElse(null);
    }

    private void proceedEmbeddedImplicitCaseForCreateConsent(CreateConsentResponse response, String psuId, String consentId) {
        aisAuthorizationService.createConsentAuthorization(psuId, consentId)
            .ifPresent(a -> {
                response.setAuthorizationId(a.getAuthorizationId());
            });
    }
}
