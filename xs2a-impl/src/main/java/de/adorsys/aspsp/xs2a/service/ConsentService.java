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
import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import de.adorsys.aspsp.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.aspsp.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.aspsp.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.PisPsuDataService;
import de.adorsys.aspsp.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.aspsp.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.aspsp.xs2a.service.validator.CreateConsentRequestValidator;
import de.adorsys.aspsp.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES;
import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;

@Service
@RequiredArgsConstructor
public class ConsentService { //TODO change format of consentRequest to mandatory obtain PSU-Id and only return data which belongs to certain PSU tobe changed upon v1.1
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;
    private final SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentDataService aisConsentDataService;
    private final AisAuthorizationService aisAuthorizationService;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final PisScaAuthorisationService pisAuthorizationService;
    private final PisPsuDataService pisPsuDataService;
    private final TppService tppService;
    private final AuthorisationMethodService authorisationMethodService;
    private final AisConsentSpi aisConsentSpi;
    private final CreateConsentRequestValidator createConsentRequestValidator;

    /**
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuData PsuIdData container of authorisation data about PSU
     * @return CreateConsentResponse representing the complete response to create consent request
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred) {
        ValidationResult validationResult = createConsentRequestValidator.validateRequest(request);

        if (validationResult.isNotValid()) {
            return ResponseObject.<CreateConsentResponse>builder().fail(validationResult.getMessageError()).build();
        }

        if (isConsentGlobal(request) || isConsentForAllAvailableAccounts(request)) {
            request.setAccess(getAccessForGlobalOrAllAvailableAccountsConsent(request));
        }

        String tppId = tppService.getTppId();
        String consentId = aisConsentService.createConsent(request, psuData, tppId);

        if (StringUtils.isBlank(consentId)) {
            return ResponseObject.<CreateConsentResponse>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.RESOURCE_UNKNOWN_400))).build();
        }

        SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(psuData);
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);

        SpiResponse<VoidResponse> initiateAisConsentSpiResponse = aisConsentSpi.initiateAisConsent(spiPsuData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(initiateAisConsentSpiResponse.getAspspConsentData());

        if (initiateAisConsentSpiResponse.hasError()) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, messageErrorCodeMapper.mapToMessageErrorCode(initiateAisConsentSpiResponse.getResponseStatus()))))
                       .build();
        }

        ResponseObject<CreateConsentResponse> createConsentResponseObject = ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(RECEIVED.getValue(), consentId, null, null, null, null)).build();

        if (aspspProfileService.getScaApproach() == ScaApproach.EMBEDDED
                && authorisationMethodService.isImplicitMethod(explicitPreferred)) {
            proceedEmbeddedImplicitCaseForCreateConsent(createConsentResponseObject.getBody(), psuData, consentId);
        }

        return createConsentResponseObject;
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     * Returns status of requested consent
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        return Optional.ofNullable(getValidatedAccountConsent(consentId))
                   .map(consent -> ResponseObject.<ConsentStatusResponse>builder().body(new ConsentStatusResponse(consent.getConsentStatus())).build())
                   .orElseGet(ResponseObject.<ConsentStatusResponse>builder()
                                  .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400)))
                                  ::build);
    }

    /**
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     * Revokes account consent on PSU request
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);

        if (accountConsent != null) {
            SpiPsuData spiPsuData = psuDataMapper.mapToSpiPsuData(accountConsent.getPsuData());
            SpiResponse<VoidResponse> revokeAisConsentResponse = aisConsentSpi.revokeAisConsent(spiPsuData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));
            aisConsentDataService.updateAspspConsentData(revokeAisConsentResponse.getAspspConsentData());

            if (revokeAisConsentResponse.hasError()) {
                return ResponseObject.<Void>builder()
                           .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, messageErrorCodeMapper.mapToMessageErrorCode(revokeAisConsentResponse.getResponseStatus()))))
                           .build();
            }

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
        AccountConsent consent = getValidatedAccountConsent(consentId);
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    public ResponseObject<AccountConsent> getValidatedConsent(String consentId, boolean withBalance) {
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);

        if (accountConsent == null) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build();
        }

        if (withBalance && !accountConsent.isWithBalance()) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_INVALID)))
                       .build();
        }

        if (LocalDate.now().compareTo(accountConsent.getValidUntil()) >= 0) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }

        if (!accountConsent.isValidStatus()) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_EXPIRED))).build();
        }
        if (!accountConsent.isValidFrequency()) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.ACCESS_EXCEEDED))).build();
        }
        return ResponseObject.<AccountConsent>builder().body(accountConsent).build();
    }

    public ResponseObject<AccountConsent> getValidatedConsent(String consentId) {
        return getValidatedConsent(consentId, false);
    }

    public ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuData, String consentId) {
        return aisAuthorizationService.createConsentAuthorization(psuData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder().fail(new MessageError(MessageErrorCode.CONSENT_UNKNOWN_400))::build);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(aisAuthorizationService.getAccountConsentAuthorizationById(updatePsuData.getAuthorizationId(), updatePsuData.getConsentId()))
                   .map(conAuth -> getUpdateConsentPsuDataResponse(updatePsuData, conAuth))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = aisAuthorizationService.updateConsentPsuData(updatePsuData, consentAuthorization);

        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getErrorCode())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(new MessageError(e))
                                               .build())
                                 .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                  ::build);
    }

    public ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> createPisConsentAuthorization(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        return pisAuthorizationService.createConsentAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                                  ::build);
    }

    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentPsuData(UpdatePisConsentPsuDataRequest request) {
        Xs2aUpdatePisConsentPsuDataResponse response = pisAuthorizationService.updateConsentPsuData(request);

        if (response.hasError()) {
            return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                   .body(response)
                   .build();
    }

    public ResponseObject<Xs2aCreatePisConsentCancellationAuthorisationResponse> createPisConsentCancellationAuthorization(String paymentId, PaymentType paymentType) {
        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        return pisAuthorizationService.createConsentCancellationAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xs2aCreatePisConsentCancellationAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xs2aCreatePisConsentCancellationAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                  ::build);
    }

    public ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getPaymentInitiationCancellationAuthorisationInformation(String paymentId) {
        return pisAuthorizationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                                  .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    public boolean isValidAccountByAccess(String resourceId, List<Xs2aAccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getResourceId().equals(resourceId));
    }

    private Boolean isNotEmptyAccess(Xs2aAccountAccess access) {
        return Optional.ofNullable(access)
                   .map(Xs2aAccountAccess::isNotEmpty)
                   .orElse(false);
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

    private AccountConsent getValidatedAccountConsent(String consentId) {
        return Optional.ofNullable(aisConsentService.getAccountConsentById(consentId))
                   .filter(consent -> tppService.getTppId().equals(consent.getTppId()))
                   .orElse(null);
    }

    private void proceedEmbeddedImplicitCaseForCreateConsent(CreateConsentResponse response, PsuIdData psuData, String consentId) {
        aisAuthorizationService.createConsentAuthorization(psuData, consentId)
            .ifPresent(a -> {
                response.setAuthorizationId(a.getAuthorizationId());
            });
    }
}
