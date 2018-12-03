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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.PisConsentStatusResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisConsentAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisConsentPsuDataResponse;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis/consent")
@Api(value = "api/v1/pis/consent", tags = "PIS, Consents", description = "Provides access to consent management system for PIS")
public class PisConsentController {
    private final PisConsentService pisConsentService;

    @PostMapping(path = "/")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CreatePisConsentResponse> createPaymentConsent(@RequestBody PisConsentRequest request) {
        return pisConsentService.createPaymentConsent(request)
                   .map(c -> new ResponseEntity<>(c, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisConsentStatusResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<PisConsentStatusResponse> getConsentStatusById(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return pisConsentService.getConsentStatusById(consentId)
                   .map(status -> new ResponseEntity<>(new PisConsentStatusResponse(status), HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PisConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<PisConsentResponse> getConsentById(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return pisConsentService.getConsentById(consentId)
                   .map(pc -> new ResponseEntity<>(pc, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateConsentStatus(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP by more values.", allowableValues = "RECEIVED,  REJECTED, VALID, REVOKED_BY_PSU,  EXPIRED,  TERMINATED_BY_TPP")
        @PathVariable("status") String status) {
        return pisConsentService.updateConsentStatusById(consentId, ConsentStatus.valueOf(status))
                   .map(updated -> new ResponseEntity<Void>(HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping(path = "/{payment-id}/authorizations")
    @ApiOperation(value = "Create consent authorization for given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreatePisConsentAuthorisationResponse> createConsentAuthorization(
        @ApiParam(name = "payment-id", value = "The consent identification assigned to the created consent authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @RequestBody PsuIdData psuData) {
        return pisConsentService.createAuthorization(paymentId, CmsAuthorisationType.CREATED, psuData)
                   .map(authorization -> new ResponseEntity<>(authorization, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Create payment authorization cancellation for given payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreatePisConsentAuthorisationResponse> createConsentAuthorizationCancellation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @RequestBody PsuIdData psuData) {
        return pisConsentService.createAuthorization(paymentId, CmsAuthorisationType.CANCELLED, psuData)
                   .map(authorization -> new ResponseEntity<>(authorization, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Update pis consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<UpdatePisConsentPsuDataResponse> updateConsentAuthorization(
        @ApiParam(name = "authorization-id", value = "The consent authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId,
        @RequestBody UpdatePisConsentPsuDataRequest request) {
        return pisConsentService.updateConsentAuthorisation(authorizationId, request)
                   .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Getting pis consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<GetPisConsentAuthorisationResponse> getConsentAuthorization(
        @ApiParam(name = "authorization-id", value = "The consent authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId) {
        return pisConsentService.getPisConsentAuthorisationById(authorizationId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/cancellation-authorisations/{cancellation-id}")
    @ApiOperation(value = "Update pis consent cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<UpdatePisConsentPsuDataResponse> updateConsentCancellationAuthorization(
        @ApiParam(name = "cancellation-id", value = "The consent cancellation authorisation identification assigned to the created cancellation authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("cancellation-id") String cancellationId,
        @RequestBody UpdatePisConsentPsuDataRequest request) {
        return pisConsentService.updateConsentCancellationAuthorisation(cancellationId, request)
                   .map(updated -> new ResponseEntity<>(updated, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/cancellation-authorisations/{cancellation-id}")
    @ApiOperation(value = "Getting pis consent cancellation authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<GetPisConsentAuthorisationResponse> getConsentAuthorizationCancellation(
        @ApiParam(name = "cancellation-id", value = "The consent cancellation authorisation identification assigned to the created cancellation authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("cancellation-id") String cancellationId) {
        return pisConsentService.getPisConsentCancellationAuthorisationById(cancellationId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/cancellation-authorisations")
    @ApiOperation(value = "Gets list of payment cancellation authorization IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getConsentAuthorisationCancellation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {
        return pisConsentService.getAuthorisationByPaymentId(paymentId, CmsAuthorisationType.CANCELLED)
                   .map(authorization -> new ResponseEntity<>(authorization, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{payment-id}/authorisations")
    @ApiOperation(value = "Gets list of payment authorization IDs by payment ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getConsentAuthorisation(
        @ApiParam(name = "payment-id", value = "The payment identification of the related payment.", example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE")
        @PathVariable("payment-id") String paymentId) {
        return pisConsentService.getAuthorisationByPaymentId(paymentId, CmsAuthorisationType.CREATED)
                   .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // TODO return correct error code in case consent was not found https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/408
    @PutMapping(path = "/{consent-id}/payment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updatePaymentConsent(@RequestBody PisConsentRequest request, @PathVariable("consent-id") String consentId) {
        pisConsentService.updatePaymentConsent(request, consentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
