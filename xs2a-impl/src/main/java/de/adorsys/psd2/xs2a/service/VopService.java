/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.BulkVop;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.vop.*;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.security.SecuringBodyMessageValidator;
import de.adorsys.psd2.xs2a.service.validator.security.SigningHttpMessageValidator;
import de.adorsys.psd2.xs2a.service.validator.vop.CreateBulkVopValidator;
import de.adorsys.psd2.xs2a.service.validator.vop.dto.CreateVopRequestObject;
import de.adorsys.psd2.xs2a.service.validator.vop.CreateVopValidator;
import de.adorsys.psd2.xs2a.service.vop.create.CreateBulkVopService;
import de.adorsys.psd2.xs2a.service.vop.create.CreateSingleVopService;
import de.adorsys.psd2.xs2a.service.vop.read.ReadBulkVopService;
import de.adorsys.psd2.xs2a.service.vop.status.ReadBulkVopStatusService;
import de.adorsys.psd2.xs2a.web.header.VopInitiationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.VopResponseHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GodClass") // VopService coordinates multiple VOP operations; refactoring would split business logic
public class VopService {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final Xs2aEventService xs2aEventService;
    private final LoggingContextService loggingContextService;
    private final CreateVopValidator createVopValidator;
    private final CreateBulkVopValidator createBulkVopValidator;
    private final CreateSingleVopService createSingleVopService;
    private final CreateBulkVopService createBulkVopService;
    private final ReadBulkVopService readBulkVopService;
    private final ReadBulkVopStatusService readBulkVopStatusService;
    private final SecuringBodyMessageValidator securingBodyMessageValidator;
    private final SigningHttpMessageValidator signingHttpMessageValidator;
    private final RequestVopProviderService requestVopProviderService;
    private boolean asyncModeInitiated = false;
    /**
     * Performs party verification for a single party with initiation parameters
     *
     * @param vop                     the VOP data as byte array
     * @param vopInitiationParameters the VOP initiation parameters
     * @return Response with the party verification result
     */
    public ResponseObject<VopInitiationResponse> performPartyVerification(byte[] vop, VopInitiationParameters vopInitiationParameters) {
        xs2aEventService.recordTppRequest(EventType.VOP_REQUEST_RECEIVED, vop);
        // Log VOP API reference information from ASPSP profile if available
        vopApiReferenceInformation();
        // Log Supported VOP variants and validate supported services
        var supportedServicesValidationResult = checkSupportedServices(vopInitiationParameters);
        if (supportedServicesValidationResult != null) {
            return supportedServicesValidationResult;
        }

        // Validate VOP request
        var createVopRequestObject = new CreateVopRequestObject(vop, vopInitiationParameters);
        var validationResult = createVopValidator.validate(createVopRequestObject);
        if (validationResult.isNotValid()) {
            log.info("party-verification-endpoint [{}]. perform party verification - validation failed: [{}]",
                    vopInitiationParameters.getPartyVerificationEndpoint(), validationResult.getMessageError());
            return getVopInitiationResponseResponseObjectFail(validationResult);
        }

        var responseObject = createSingleVopService.createVerification(vop, vopInitiationParameters);
        if (responseObject.hasError()) {
            log.info("Perform verification failed: [{}]", responseObject.getError());
            return getVopInitiationResponseResponseObjectFail(validationResult);
        }
        var vopInitiationResponse = responseObject.getBody();
        loggingContextService.storeVerificationStatus(vopInitiationResponse.getPartyIdMatch(), vopInitiationResponse.getPartyNameMatch());
        return ResponseObject.<VopInitiationResponse>builder()
                .body(vopInitiationResponse)
                .build();
    }


    public ResponseObject<Object> initiateBulkPartyVerification(byte[] bulkVop, VopInitiationParameters vopInitiationParameters) {
        xs2aEventService.recordTppRequest(EventType.VOP_BULK_REQUEST_RECEIVED, bulkVop);

        // Log VOP API reference information from ASPSP profile if available
        vopApiReferenceInformation();
        //Log Supported bulk batch processing modes:
        var supportedBulkBatchProcessingModes = aspspProfileService.getBulkBatchProcessingSupported();
        log.info("Supported bulk batch processing modes: [{}]", supportedBulkBatchProcessingModes);
        // Log Supported VOP variants and validate supported services
        var supportedServicesValidationResult = checkSupportedServices(vopInitiationParameters);
        if (supportedServicesValidationResult != null) {
            log.info("unsupported service - perform bulk party verification failed : [{}]", supportedServicesValidationResult.getError());
            return ResponseObject.builder()
                .fail(supportedServicesValidationResult.getError())
                .build();
        }

        // Check if the client wants to proceed with bulkBatchProcessing asynchronous-mode and if it is supported by the ASPSP
        var supportedBatchProcessingAsyncMode = checkSupportedBatchProcessingAsyncMode(vopInitiationParameters.getClientAsynchronousProcessPreferred(),
                                                                                        supportedBulkBatchProcessingModes
        );
        if (supportedBatchProcessingAsyncMode != null) {
            log.info("unsupported service - perform bulk party verification failed: [{}]", supportedBatchProcessingAsyncMode.getError());
            return ResponseObject.builder()
                .fail(supportedBatchProcessingAsyncMode.getError())
                .build();
        }


        return asyncModeInitiated
            ?initiateBatchProcessingAsynchronousMode(bulkVop, vopInitiationParameters)
            :initiateBatchProcessingSynchronousMode(bulkVop, vopInitiationParameters);
    }

    /**
     * Retrieves the status of a bulk party verification
     *
     * @param bulkVopInitiationParameters The bulk verification ID
     * @return Response with the bulk verification status
     */
    public ResponseObject<GetCommonVopStatusResponse> getBulkPartyVerificationStatus(BulkVopInitiationParameters bulkVopInitiationParameters, String bulkPartyVerificationEndpoint) {
        xs2aEventService.recordVopTppRequest(Objects.requireNonNull(bulkVopInitiationParameters.getInstanceId()),  EventType.GET_VOP_BULK_REQUEST_RECEIVED);

        var validationResult = validateEndpoint(bulkPartyVerificationEndpoint);
        if (validationResult.isNotValid()) {
            log.info("bulk-party-verification-endpoint [{}]. read bulk party verification status - process failed: [{}]",
                bulkPartyVerificationEndpoint, validationResult.getMessageError());
            return ResponseObject.<GetCommonVopStatusResponse>builder()
                .fail(validationResult.getMessageError())
                .build();
        }

        var supportedSigningHttpMessageValidationResult = validateSigningHttpMessage(bulkVopInitiationParameters, bulkPartyVerificationEndpoint);
        if (supportedSigningHttpMessageValidationResult != null) {
            log.info("unsupported service - read bulk party verification status failed : [{}]", supportedSigningHttpMessageValidationResult.getError());
            return ResponseObject.<GetCommonVopStatusResponse>builder()
                .fail(supportedSigningHttpMessageValidationResult.getError())
                .build();
        }

        ResponseObject<GetCommonVopStatusResponse> response = readBulkVopStatusService.getVopStatus(bulkVopInitiationParameters.getInstanceId());

        if (response.hasError()) {
            log.info("Bulk-Vop-ID [{}]. Read Bulk Vop Status failed: {}", bulkVopInitiationParameters.getInstanceId(), response.getError());
            return ResponseObject.<GetCommonVopStatusResponse>builder()
                .fail(response.getError())
                .build();
        }

        GetCommonVopStatusResponse getCommonVopStatusResponse = response.getBody();
        loggingContextService.storeGetBulkVopStatusIdAndStatus(getCommonVopStatusResponse.getVopBulkId(), getCommonVopStatusResponse.getVopBulkStatus());

        return ResponseObject.<GetCommonVopStatusResponse>builder()
            .body(getCommonVopStatusResponse)
            .build();
    }

    /**
     * Retrieves the result of a bulk party verification
     *
     * @param bulkVopInitiationParameters The bulk verification ID
     * @param bulkPartyVerificationEndpoint The bulk verification endpoint to be used for retrieving the result of the verification.
     * @return Response with the bulk verification result
     */
    public ResponseObject<GetCommonVopResponse> getBulkPartyVerificationResult(BulkVopInitiationParameters bulkVopInitiationParameters, String bulkPartyVerificationEndpoint) {
        xs2aEventService.recordVopTppRequest(Objects.requireNonNull(bulkVopInitiationParameters.getInstanceId()),  EventType.GET_VOP_BULK_REQUEST_RECEIVED);

        var validationResult = validateEndpoint(bulkPartyVerificationEndpoint);
        if (validationResult.isNotValid()) {
            log.info("bulk-party-verification-endpoint [{}]. read bulk party verification - process failed: [{}]",
                bulkPartyVerificationEndpoint, validationResult.getMessageError());
            return ResponseObject.<GetCommonVopResponse>builder()
                .fail(validationResult.getMessageError())
                .build();
        }

        var supportedSigningHttpMessageValidationResult = validateSigningHttpMessage(bulkVopInitiationParameters, bulkPartyVerificationEndpoint);
        if (supportedSigningHttpMessageValidationResult != null) {
            log.info("unsupported service - read bulk party verification result failed : [{}]", supportedSigningHttpMessageValidationResult.getError());
            return ResponseObject.<GetCommonVopResponse>builder()
                .fail(supportedSigningHttpMessageValidationResult.getError())
                .build();
        }

        ResponseObject<GetCommonVopResponse> response = readBulkVopService.getVopResult(bulkVopInitiationParameters.getInstanceId());

        if (response.hasError()) {
            log.info("Bulk-Vop-ID [{}]. Read Bulk Vop failed: {}", bulkVopInitiationParameters.getInstanceId(), response.getError());
            return ResponseObject.<GetCommonVopResponse>builder()
                .fail(response.getError())
                .build();
        }

        GetCommonVopResponse getCommonVopResponse = response.getBody();
        loggingContextService.storeGetBulkVopIdAndStatus(getCommonVopResponse.getVopBulkId(), getCommonVopResponse.getVopBulkStatus());

        return ResponseObject.<GetCommonVopResponse>builder()
            .body(getCommonVopResponse)
            .build();
    }

    /**
     * Performs creditor confirmation for a payment
     * <p>
     * Note: This functionality is not yet implemented.
     * The Creditor Confirmation service allows verification of payment creditor details
     * but is currently not supported by this implementation.
     *
     * @param paymentService The payment service
     * @param paymentProduct The payment product
     * @param paymentId      The payment ID
     * @return Response with SERVICE_NOT_SUPPORTED error (HTTP 406)
     */
    public ResponseObject<CreditorConfirmationResponse> performCreditorConfirmation(
        String paymentService, String paymentProduct, String paymentId) {
        log.warn("Creditor Confirmation service is not yet supported. Payment service: [{}], product: [{}], ID: [{}]",
            paymentService, paymentProduct, paymentId);

        xs2aEventService.recordTppRequest(EventType.VOP_REQUEST_RECEIVED, new byte[0]);

        return ResponseObject.<CreditorConfirmationResponse>builder()
            .fail(new MessageError(
                ErrorType.VOP_406,
                TppMessageInformation.of(
                    MessageErrorCode.SERVICE_NOT_SUPPORTED,
                    "Creditor Confirmation service is not yet implemented"
                )
            ))
            .build();
    }

    private ResponseObject<Object> initiateBatchProcessingSynchronousMode(byte[] bulkVop, VopInitiationParameters vopInitiationParameters) {
        var createVopRequestObject = new CreateVopRequestObject(bulkVop, vopInitiationParameters);
        var validationResult = createBulkVopValidator.validate(createVopRequestObject);
        if (validationResult.isNotValid()) {
            log.info("bulk-party-verification-endpoint [{}]. perform bulk party verification - validation failed: [{}]",
                vopInitiationParameters.getBulkPartyVerificationEndpoint(), validationResult.getMessageError());
            return ResponseObject.builder()
                .fail(validationResult.getMessageError())
                .build();
        }

        var responseObject = createBulkVopService.createVerification(bulkVop, vopInitiationParameters);
        if (responseObject.hasError()) {
            log.info("perform bulk party verification failed: [{}]", responseObject.getError());
            return ResponseObject.builder()
                .fail(responseObject.getError())
                .build();
        }

        var vopInitiationResponse = responseObject.getBody();
        loggingContextService.storeBulkVerificationStatus(vopInitiationResponse.getVopBulkStatus(), vopInitiationResponse.getPartyVerificationResponses());
        return ResponseObject.builder()
            .body(vopInitiationResponse)
            .build();
    }

    @SuppressWarnings("PMD.UnusedFormalParameter") // bulkVop parameter reserved for future asynchronous implementation
    private ResponseObject<Object> initiateBatchProcessingAsynchronousMode(byte[] bulkVop, VopInitiationParameters vopInitiationParameters) {
        log.warn("Asynchronous batch processing is not yet implemented. Bulk-party-verification-endpoint: [{}]",
            vopInitiationParameters.getBulkPartyVerificationEndpoint());

        return ResponseObject.builder()
            .fail(new MessageError(
                ErrorType.VOP_406,
                TppMessageInformation.of(
                    MessageErrorCode.SERVICE_NOT_SUPPORTED,
                    "Asynchronous batch processing mode is not yet implemented"
                )
            ))
            .build();
    }

    private @Nullable ResponseObject<VopInitiationResponse> checkSupportedServices(VopInitiationParameters vopInitiationParameters) {
        //Log Supported VOP variants:
        log.info("Supported VOP variants: [{}]", aspspProfileService.getVariantSupported());

        //Select between single and bulk VOP based on the presence of bulkPartyVerificationEndpoint
        String vopEndpoint = (vopInitiationParameters.getBulkPartyVerificationEndpoint() == null)
                                                    ? vopInitiationParameters.getPartyVerificationEndpoint()
                                                    : vopInitiationParameters.getBulkPartyVerificationEndpoint();

        // Validate signing HTTP message
        ResponseObject<VopInitiationResponse> signingValidationResult = validateSigningHttpMessage(vopInitiationParameters, vopEndpoint);
        if (signingValidationResult != null) {
            return signingValidationResult;
        }

        // Validate securing body message
        ResponseObject<VopInitiationResponse> securingValidationResult = validateSecuringBodyMessage(vopInitiationParameters, vopEndpoint);
        if (securingValidationResult != null) {
            return securingValidationResult;
        }
        return null;
    }


    private ResponseObject<VopInitiationResponse> validateSigningHttpMessage(VopInitiationParameters vopInitiationParameters, String vopEndpoint) {
        var signingHttpMessage = vopInitiationParameters.getSigningHttpMessage();
        boolean hasSigningData = signingHttpMessage.digest().isPresent() || signingHttpMessage.xJwsSignature().isPresent();

        if (checkSupportedSigningHttpMessage(hasSigningData)) {
            return ResponseObject.<VopInitiationResponse>builder()
                .fail(new MessageError(ErrorType.VOP_406,
                    TppMessageInformation.of(MessageErrorCode.SERVICE_NOT_SUPPORTED)))
                .build();
        }

        if (Boolean.TRUE.equals(aspspProfileService.isSigningHttpMessageSupported()) && hasSigningData) {
            log.info("Signing Http Request Message service is supported by ASPSP, and signing data was provided");
            var validationResult = signingHttpMessageValidator.validate(signingHttpMessage);
            if (validationResult.isNotValid()) {
                log.info("party-verification-endpoint [{}]. perform party verification - Signing Http Request Message validation failed: [{}]",
                    vopEndpoint, validationResult.getMessageError());
                return getVopInitiationResponseResponseObjectFail(validationResult);
            }
        }

        return null;
    }

    private ResponseObject<VopInitiationResponse> validateSecuringBodyMessage(VopInitiationParameters vopInitiationParameters, String vopEndpoint) {
        var securingBodyMessage = vopInitiationParameters.getSecuringBodyMessage();
        boolean hasSecuringBodyData = securingBodyMessage.bodySigProfile().isPresent()
            || securingBodyMessage.bodyEncProfile().isPresent()
            || securingBodyMessage.bodyEncList().isPresent();

        if (checkSupportedSecuringBodyMessage(hasSecuringBodyData)) {
            return ResponseObject.<VopInitiationResponse>builder()
                .fail(new MessageError(ErrorType.VOP_406,
                    TppMessageInformation.of(MessageErrorCode.SERVICE_NOT_SUPPORTED)))
                .build();
        }

        if (Boolean.TRUE.equals(aspspProfileService.isSecuringBodyMessageSupported()) && hasSecuringBodyData) {
            log.info("Securing Body Message service is supported by ASPSP, and securing body data was provided");
            var validationResult = securingBodyMessageValidator.validate(securingBodyMessage);
            if (checkSecuringBodyMessageValidation(vopEndpoint, validationResult)) {
                return getVopInitiationResponseResponseObjectFail(validationResult);
            }
        }

        return null;
    }
    private static boolean checkSecuringBodyMessageValidation(String vopEndpoint, ValidationResult validationResult) {
        if (validationResult.isNotValid()) {
            log.info("party-verification-endpoint [{}]. perform party verification - Securing Body Message validation failed: [{}]",
                vopEndpoint, validationResult.getMessageError());
            return true;
        }
        return false;

    }
    private boolean checkSupportedSecuringBodyMessage(boolean hasSecuringBodyData) {
        if (Boolean.FALSE.equals(aspspProfileService.isSecuringBodyMessageSupported()) && hasSecuringBodyData) {
            log.info("Securing Body Message service is not supported by ASPSP, but securing body data was provided");
            return true;
        }
        return false;

    }
    private boolean checkSupportedSigningHttpMessage(boolean hasSigningData) {
        if (Boolean.FALSE.equals(aspspProfileService.isSigningHttpMessageSupported()) && hasSigningData) {
            log.warn("Signing Http Request Message service is not supported by ASPSP, but signing data was provided");
            return true;
        }
        return false;
    }
    private void vopApiReferenceInformation() {
        String xReferenceApiName = aspspProfileService.getXReferenceApiName();
        String xReferenceApiDocument = aspspProfileService.getXReferenceApiDocument();
        String xReferenceApiVersion = aspspProfileService.getXReferenceApiVersion();

        if (xReferenceApiName != null && xReferenceApiDocument != null && xReferenceApiVersion != null) {
            log.info("VOP API Reference - Name: {}, Document: {}, Version: {}",
                xReferenceApiName, xReferenceApiDocument, xReferenceApiVersion);
        }
    }

    private static ResponseObject<VopInitiationResponse> getVopInitiationResponseResponseObjectFail(ValidationResult validationResult) {
        return ResponseObject.<VopInitiationResponse>builder()
            .fail(validationResult.getMessageError())
            .build();
    }

    private ValidationResult validateEndpoint(String bulkEndpoint) {

        // Validate endpoint against BulkVop enum values
        if (bulkEndpoint != null && BulkVop.getByValue(bulkEndpoint).isPresent()) {
            return ValidationResult.valid();
        }

        return ValidationResult.invalid(
            ErrorType.VOP_400,
            TppMessageInformation.buildWithCustomError(
                MessageErrorCode.FORMAT_ERROR,
                "Unsupported bulkPartyVerificationEndpoint"
            )
        );
    }

    private @Nullable ResponseObject<Object> checkSupportedBatchProcessingAsyncMode(
        Optional<Boolean> clientAsynchronousProcessPreferred,
        List<String> supportedBulkBatchProcessingModes) {

        // If client preference is not provided or empty, no validation needed
        if (clientAsynchronousProcessPreferred.isEmpty()) {
            log.debug("Client asynchronous process preference not provided, skipping async mode validation");
            return null;
        }

        boolean clientPrefersAsync = clientAsynchronousProcessPreferred.get();

        // If client prefers synchronous mode (false), no need to check async support
        if (!clientPrefersAsync) {
            log.debug("Client prefers synchronous processing mode");
            return null;
        }

        // Client prefers asynchronous mode - check if ASPSP supports it
        if (supportedBulkBatchProcessingModes == null || supportedBulkBatchProcessingModes.isEmpty()) {
            log.warn("Client prefers asynchronous mode but no bulk batch processing modes are configured in ASPSP profile");
            return ResponseObject.builder()
                .fail(new MessageError(
                    ErrorType.VOP_406,
                    TppMessageInformation.of(
                        MessageErrorCode.SERVICE_NOT_SUPPORTED,
                        "Asynchronous bulk batch processing is not supported by the ASPSP"
                    )
                ))
                .build();
        }

        boolean asyncModeSupported = supportedBulkBatchProcessingModes.stream()
            .anyMatch("asynchronous-mode"::equalsIgnoreCase);

        if (!asyncModeSupported) {
            log.info("Client prefers asynchronous processing but ASPSP only supports: [{}]",
                supportedBulkBatchProcessingModes);
            return ResponseObject.builder()
                .fail(new MessageError(
                    ErrorType.VOP_406,
                    TppMessageInformation.of(
                        MessageErrorCode.SERVICE_NOT_SUPPORTED,
                        "Asynchronous bulk batch processing mode is not supported by this ASPSP"
                    )
                ))
                .build();
        }

        log.debug("Client prefers asynchronous mode and ASPSP supports it");
        asyncModeInitiated = true;
        return null;
    }

    public VopResponseHeaders buildVopInitiationResponseHeaders() {
        // Get the X-Request-ID from the request provider service
        String requestId = requestVopProviderService.getInternalRequestIdString();

        // Build VOP headers using the VopInitiationHeadersBuilder
        return new VopInitiationHeadersBuilder( aspspProfileService.getXReferenceApiName(),
                                                aspspProfileService.getXReferenceApiVersion(),
                                                aspspProfileService.getXReferenceApiDocument())
                                                                    .buildInitiateVopHeaders(requestId);
    }


}
