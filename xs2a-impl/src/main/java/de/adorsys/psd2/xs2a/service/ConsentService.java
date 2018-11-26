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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodService;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.CreateConsentRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.RECEIVED;
import static de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS;
import static de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccessType.ALL_ACCOUNTS_WITH_BALANCES;

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
    private final Xs2aEventService xs2aEventService;

    /**
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     *
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuData PsuIdData container of authorisation data about PSU
     * @return CreateConsentResponse representing the complete response to create consent request
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred) {
        xs2aEventService.recordTppRequest(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, request);
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
     * Returns status of requested consent
     *
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);

        AccountConsent validatedAccountConsent = getValidatedAccountConsent(consentId);
        Optional<ConsentStatus> consentStatus =
            Optional.ofNullable(validatedAccountConsent)
                .map(AccountConsent::getConsentStatus);

        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();
        if (consentStatus.isPresent()) {
            responseBuilder = responseBuilder.body(new ConsentStatusResponse(consentStatus.get()));
        } else {
            responseBuilder = responseBuilder.fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400)));
        }
        return responseBuilder.build();
    }

    /**
     * Revokes account consent on PSU request
     *
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
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
     * Returns account consent by its id
     *
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);

        AccountConsent consent = getValidatedAccountConsent(consentId);
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(new MessageError(new TppMessageInformation(MessageCategory.ERROR, MessageErrorCode.CONSENT_UNKNOWN_400))).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    ResponseObject<AccountConsent> getValidatedConsent(String consentId, boolean withBalance) {
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

    ResponseObject<AccountConsent> getValidatedConsent(String consentId) {
        return getValidatedConsent(consentId, false);
    }

    public ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuData, String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        return aisAuthorizationService.createConsentAuthorization(psuData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder().fail(new MessageError(MessageErrorCode.CONSENT_UNKNOWN_400))::build);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        xs2aEventService.recordAisTppRequest(updatePsuData.getConsentId(), EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updatePsuData);

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

    // TODO extract this method to PaymentAuthorisationService https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/507
    public ResponseObject<Xsa2CreatePisConsentAuthorisationResponse> createPisConsentAuthorization(String paymentId, PaymentType paymentType, PsuIdData psuData) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        return pisAuthorizationService.createConsentAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xsa2CreatePisConsentAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.PAYMENT_FAILED))
                                  ::build);
    }

    // TODO extract this method to PaymentAuthorisationService https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/507
    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentPsuData(Xs2aUpdatePisConsentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), EventType.UPDATE_PAYMENT_AUTHORISATION_PSU_DATA_REQUEST_RECEIVED, request);
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

    // TODO extract this method to PaymentCancellationAuthorisationService https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/507
    public ResponseObject<Xs2aCreatePisConsentCancellationAuthorisationResponse> createPisConsentCancellationAuthorization(String paymentId, PaymentType paymentType) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.START_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        PsuIdData psuData = pisPsuDataService.getPsuDataByPaymentId(paymentId);
        return pisAuthorizationService.createConsentCancellationAuthorisation(paymentId, paymentType, psuData)
                   .map(resp -> ResponseObject.<Xs2aCreatePisConsentCancellationAuthorisationResponse>builder()
                                    .body(resp)
                                    .build())
                   .orElseGet(ResponseObject.<Xs2aCreatePisConsentCancellationAuthorisationResponse>builder()
                                  .fail(new MessageError(MessageErrorCode.FORMAT_ERROR))
                                  ::build);
    }

    // TODO extract this method to PaymentCancellationAuthorisationService https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/507
    public ResponseObject<Xs2aUpdatePisConsentPsuDataResponse> updatePisConsentCancellationPsuData(Xs2aUpdatePisConsentPsuDataRequest request) {
        xs2aEventService.recordPisTppRequest(request.getPaymentId(), EventType.UPDATE_PAYMENT_CANCELLATION_PSU_DATA_REQUEST_RECEIVED, request);

        Xs2aUpdatePisConsentPsuDataResponse response = pisAuthorizationService.updateConsentCancellationPsuData(request);

        if (response.hasError()) {
            return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                       .fail(new MessageError(response.getErrorHolder().getErrorCode(), response.getErrorHolder().getMessage()))
                       .build();
        }
        return ResponseObject.<Xs2aUpdatePisConsentPsuDataResponse>builder()
                   .body(response)
                   .build();
    }

    // TODO extract this method to PaymentCancellationAuthorisationService https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/507
    public ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> getPaymentInitiationCancellationAuthorisationInformation(String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_CANCELLATION_AUTHORISATION_REQUEST_RECEIVED);

        return pisAuthorizationService.getCancellationAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aPaymentCancellationAuthorisationSubResource>builder()
                                  .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    public ResponseObject<Xs2aPaymentAuthorisationSubResource> getPaymentInitiationAuthorisation(String paymentId) {
        xs2aEventService.recordPisTppRequest(paymentId, EventType.GET_PAYMENT_AUTHORISATION_REQUEST_RECEIVED);

        return pisAuthorizationService.getAuthorisationSubResources(paymentId)
                   .map(resp -> ResponseObject.<Xs2aPaymentAuthorisationSubResource>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aPaymentAuthorisationSubResource>builder()
                                  .fail(new MessageError(MessageErrorCode.RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    boolean isValidAccountByAccess(String resourceId, List<Xs2aAccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getResourceId().equals(resourceId));
    }

    private boolean isNotEmptyAccess(Xs2aAccountAccess access) {
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
            .ifPresent(a -> response.setAuthorizationId(a.getAuthorizationId()));
    }
}
