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

package de.adorsys.psd2.xs2a.web.controller.psd2;

import java.util.Optional;
import java.util.UUID;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import de.adorsys.psd2.xs2a.domain.vop.*;
import de.adorsys.psd2.xs2a.service.RequestVopProviderService;
import de.adorsys.psd2.xs2a.web.header.VopResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.VopModelMapperXs2a;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.adorsys.psd2.api.VerificationOfPartyApi;
import de.adorsys.psd2.model.BulkVerificationOfPartyRequest;
import de.adorsys.psd2.model.CreditorConfirmation200Response;
import de.adorsys.psd2.model.GetBulkVerificationOfParty200Response;
import de.adorsys.psd2.model.GetBulkVerificationOfPartyStatus200Response;
import de.adorsys.psd2.model.VerificationOfParty200Response;
import de.adorsys.psd2.model.VerificationOfPartyRequest;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.service.VopService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.mapper.VopModelMapperPsd2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unchecked")
@Slf4j
@RestController
@AllArgsConstructor
public class VopController implements VerificationOfPartyApi {
    private final RequestVopProviderService requestVopProviderService;
    private final VopModelMapperXs2a vopModelMapperXs2a;

    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final VopService xs2aVopService;
    private final VopModelMapperPsd2 vopModelMapperPsd2;

    @Override
    public ResponseEntity<VerificationOfParty200Response> verificationOfParty(
                                                                               String partyVerificationEndpoint,
                                                                               UUID xRequestID,
                                                                               VerificationOfPartyRequest verificationOfPartyRequest,
                                                                               Optional<String> digest,
                                                                               Optional<String> xJwsSignature,
                                                                               Optional<String> bodySigProfile,
                                                                               Optional<String> bodyEncProfile,
                                                                               Optional<String> bodyEncList,
                                                                               Optional<String> acceptLanguage,
                                                                               Optional<UUID> apIContractID) {

        var signingHttpRequestMessage = new SigningHttpMessage(digest, xJwsSignature);
        var sucuringBodyMessage = new SecuringBodyMessage(bodySigProfile, bodyEncProfile, bodyEncList);

        var vopInitiationParameters = buildSingleVopInitiationParameters(requestVopProviderService.getInstanceId(), partyVerificationEndpoint, acceptLanguage, signingHttpRequestMessage, sucuringBodyMessage);
        // Call service
        ResponseObject<VopInitiationResponse> serviceResponse = xs2aVopService.performPartyVerification(vopModelMapperXs2a.mapToXs2aVop(), vopInitiationParameters);

        // Handle response
        if (serviceResponse.hasError()) {
            MessageError error = serviceResponse.getError();
            // HTTP 406 should return empty body according to PSD2 spec
            if (error.getErrorType() == ErrorType.VOP_406) {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            return responseErrorMapper.generateErrorResponse(error);
        }

        var serviceResponseBody = serviceResponse.getBody();
        VopResponseHeaders vopResponseHeaders = xs2aVopService.buildVopInitiationResponseHeaders();

        return responseMapper.created(ResponseObject
            .builder()
            .body(vopModelMapperPsd2.mapToVerificationOfParty200Response(serviceResponseBody))
            .build(), vopResponseHeaders);

    }

    @Override
    public ResponseEntity<Object> bulkVerificationOfParty(
                                                           String bulkPartyVerificationEndpoint,
                                                           UUID xRequestID,
                                                           BulkVerificationOfPartyRequest bulkVerificationOfPartyRequest,
                                                           Optional<String> digest,
                                                           Optional<String> xJwsSignature,
                                                           Optional<String> bodySigProfile,
                                                           Optional<String> bodyEncProfile,
                                                           Optional<String> bodyEncList,
                                                           Optional<Boolean> clientAsynchronousProcessPreferred,
                                                           Optional<UUID> apIContractID) {

        var signingHttpRequestMessage = new SigningHttpMessage(digest, xJwsSignature);
        var sucuringBodyMessage = new SecuringBodyMessage(bodySigProfile, bodyEncProfile, bodyEncList);

        var vopInitiationParameters = buildBulkVopInitiationParameters(requestVopProviderService.getInstanceId(), bulkPartyVerificationEndpoint, clientAsynchronousProcessPreferred, signingHttpRequestMessage, sucuringBodyMessage);
        // Call service
        ResponseObject<Object> serviceResponse = xs2aVopService.initiateBulkPartyVerification(vopModelMapperXs2a.mapToXs2aVop(), vopInitiationParameters);

        // Handle response
        if (serviceResponse.hasError()) {
            MessageError error = serviceResponse.getError();
            // HTTP 406 should return empty body according to PSD2 spec
            if (error.getErrorType() == ErrorType.VOP_406) {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        var serviceResponseBody = serviceResponse.getBody();
        VopResponseHeaders vopResponseHeaders = xs2aVopService.buildVopInitiationResponseHeaders();

        return responseMapper.created(ResponseObject
            .builder()
            .body(vopModelMapperPsd2.mapToBulkVerificationOfParty200Response((VopInitiationResponse) serviceResponseBody))
            .build(), vopResponseHeaders);
    }

    @Override
    public ResponseEntity<GetBulkVerificationOfPartyStatus200Response> getBulkVerificationOfPartyStatus(
                                                                                                         String bulkPartyVerificationEndpoint,
                                                                                                         UUID vopBulkId,
                                                                                                         UUID xRequestID,
                                                                                                         Optional<String> digest,
                                                                                                         Optional<String> xJwsSignature,
                                                                                                         Optional<UUID> apIContractID) {
        var signingHttpRequestMessage = new SigningHttpMessage(digest, xJwsSignature);
        var bulkVopInitiationParameters = new BulkVopInitiationParameters();
        bulkVopInitiationParameters.setInstanceId(vopBulkId.toString());
        bulkVopInitiationParameters.setSigningHttpMessage(signingHttpRequestMessage);

        ResponseObject<GetCommonVopStatusResponse> serviceResponse = xs2aVopService.getBulkPartyVerificationStatus(bulkVopInitiationParameters, bulkPartyVerificationEndpoint);


        // Handle response
        if (serviceResponse.hasError()) {
            MessageError error = serviceResponse.getError();
            // HTTP 406 should return empty body according to PSD2 spec
            if (error.getErrorType() == ErrorType.VOP_406) {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        VopResponseHeaders vopResponseHeaders = xs2aVopService.buildVopInitiationResponseHeaders();
        return responseMapper.ok(serviceResponse, vopModelMapperPsd2::mapToGetBulkVerificationOfPartyStatus200Response, vopResponseHeaders);
    }

    @Override
    public ResponseEntity<GetBulkVerificationOfParty200Response> getBulkVerificationOfParty(
                                                                                             String bulkPartyVerificationEndpoint,
                                                                                             UUID vopBulkId,
                                                                                             UUID xRequestID,
                                                                                             Optional<String> digest,
                                                                                             Optional<String> xJwsSignature,
                                                                                             Optional<UUID> apIContractID) {

        var signingHttpRequestMessage = new SigningHttpMessage(digest, xJwsSignature);
        var bulkVopInitiationParameters = new BulkVopInitiationParameters();
        bulkVopInitiationParameters.setInstanceId(vopBulkId.toString());
        bulkVopInitiationParameters.setSigningHttpMessage(signingHttpRequestMessage);

        ResponseObject<GetCommonVopResponse> serviceResponse = xs2aVopService.getBulkPartyVerificationResult(bulkVopInitiationParameters, bulkPartyVerificationEndpoint);

        // Handle response
        if (serviceResponse.hasError()) {
            MessageError error = serviceResponse.getError();
            // HTTP 406 should return empty body according to PSD2 spec
            if (error.getErrorType() == ErrorType.VOP_406) {
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        VopResponseHeaders vopResponseHeaders = xs2aVopService.buildVopInitiationResponseHeaders();
        return responseMapper.ok(serviceResponse, vopModelMapperPsd2::mapToGetBulkVerificationOfParty200Response, vopResponseHeaders);
    }

    @Override
    public ResponseEntity<CreditorConfirmation200Response> creditorConfirmation(
                                                                                String paymentService,
                                                                                String paymentProduct,
                                                                                String paymentId,
                                                                                UUID xRequestID,
                                                                                Optional<String> digest,
                                                                                Optional<String> xJwsSignature) {

        ResponseObject<CreditorConfirmationResponse> serviceResponse = xs2aVopService.performCreditorConfirmation(paymentService,paymentProduct,paymentId);

        return serviceResponse.hasError()
            ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
            : responseMapper.ok(serviceResponse, vopModelMapperPsd2::mapToCreditorConfirmation200Response);
    }

    // ============================================================================
    // Override the default interface methods to implement path resolution
    // ============================================================================

    /**
     * Override the default single VOP method to handle path resolution.
     * Routes between single and bulk operations based on path analysis.
     * Uses regex pattern to match only non-bulk endpoints.
     */
    @PostMapping(
        value = "/v1/{party-verification-endpoint:(?!bulk-).*}",
        produces = {"application/json", "application/problem+json"},
        consumes = {"application/json"}
    )
    @Override
    public ResponseEntity<VerificationOfParty200Response> _verificationOfParty(
        @PathVariable("party-verification-endpoint") String partyVerificationEndpoint,
        @RequestHeader("X-Request-ID") UUID xRequestID,
        @RequestBody VerificationOfPartyRequest verificationOfPartyRequest,
        @RequestHeader(value = "Digest", required = false) Optional<String> digest,
        @RequestHeader(value = "X-JWS-Signature", required = false) Optional<String> xJwsSignature,
        @RequestHeader(value = "Body-Sig-Profile", required = false) Optional<String> bodySigProfile,
        @RequestHeader(value = "Body-Enc-Profile", required = false) Optional<String> bodyEncProfile,
        @RequestHeader(value = "Body-Enc-List", required = false) Optional<String> bodyEncList,
        @RequestHeader(value = "Accept-Language", required = false) Optional<String> acceptLanguage,
        @RequestHeader(value = "API-Contract-ID", required = false) Optional<UUID> apIContractID) {

        log.info("Processing single VOP request for endpoint: {} with X-Request-ID: {}", partyVerificationEndpoint, xRequestID);

        // Route to actual single VOP implementation
        return verificationOfParty(partyVerificationEndpoint, xRequestID, verificationOfPartyRequest,
            digest, xJwsSignature, bodySigProfile, bodyEncProfile, bodyEncList,
            acceptLanguage, apIContractID);
    }

    /**
     * Override the default bulk VOP method to handle path resolution.
     * Routes between single and bulk operations based on path analysis.
     * Uses regex pattern to match only bulk endpoints (starting with "bulk-").
     */
    @PostMapping(
        value = "/v1/{bulk-party-verification-endpoint:bulk-.*}",
        produces = {"application/json", "application/problem+json"},
        consumes = {"application/json"}
    )
    @Override
    public ResponseEntity<Object> _bulkVerificationOfParty(
        @PathVariable("bulk-party-verification-endpoint") String bulkPartyVerificationEndpoint,
        @RequestHeader("X-Request-ID") UUID xRequestID,
        @RequestBody BulkVerificationOfPartyRequest bulkVerificationOfPartyRequest,
        @RequestHeader(value = "Digest", required = false) Optional<String> digest,
        @RequestHeader(value = "X-JWS-Signature", required = false) Optional<String> xJwsSignature,
        @RequestHeader(value = "Body-Sig-Profile", required = false) Optional<String> bodySigProfile,
        @RequestHeader(value = "Body-Enc-Profile", required = false) Optional<String> bodyEncProfile,
        @RequestHeader(value = "Body-Enc-List", required = false) Optional<String> bodyEncList,
        @RequestHeader(value = "Client-Asynchronous-Process-Preferred", required = false) Optional<Boolean> clientAsynchronousProcessPreferred,
        @RequestHeader(value = "API-Contract-ID", required = false) Optional<UUID> apIContractID) {

        log.info("Processing bulk VOP request for endpoint: {} with X-Request-ID: {}", bulkPartyVerificationEndpoint, xRequestID);

        // Route to actual bulk VOP implementation
        return bulkVerificationOfParty(bulkPartyVerificationEndpoint, xRequestID, bulkVerificationOfPartyRequest,
            digest, xJwsSignature, bodySigProfile, bodyEncProfile, bodyEncList,
            clientAsynchronousProcessPreferred, apIContractID);
    }

    private SingleVopInitiationParameters buildSingleVopInitiationParameters(
            String instanceId,
            String partyVerificationEndpoint,
            Optional<String> acceptLanguage,
            SigningHttpMessage signingHttpMessage,
            SecuringBodyMessage securingBodyMessage) {

        SingleVopInitiationParameters parameters = new SingleVopInitiationParameters();
        parameters.setInstanceId(instanceId);
        parameters.setPartyVerificationEndpoint(partyVerificationEndpoint);
        parameters.setAcceptLanguage(acceptLanguage);
        parameters.setSigningHttpMessage(signingHttpMessage);
        parameters.setSecuringBodyMessage(securingBodyMessage);

        return parameters;
    }

    private BulkVopInitiationParameters buildBulkVopInitiationParameters(
            String instanceId,
            String bulkPartyVerificationEndpoint,
            Optional<Boolean> clientAsynchronousProcessPreferred,
            SigningHttpMessage signingHttpMessage,
            SecuringBodyMessage securingBodyMessage) {

        BulkVopInitiationParameters parameters = new BulkVopInitiationParameters();
        parameters.setInstanceId(instanceId);
        parameters.setBulkPartyVerificationEndpoint(bulkPartyVerificationEndpoint);
        parameters.setClientAsynchronousProcessPreferred(clientAsynchronousProcessPreferred);
        parameters.setSigningHttpMessage(signingHttpMessage);
        parameters.setSecuringBodyMessage(securingBodyMessage);

        return parameters;
    }

}
