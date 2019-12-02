/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuAuthorisation;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "psu-api/v1/payment")
@Api(value = "psu-api/v1/payment", tags = {"PSU PIS Payment"})
public class CmsPsuPisController {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    private final CmsPsuPisService cmsPsuPisService;

    @PutMapping(path = "/authorisation/{authorisation-id}/psu-data")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisCommonPaymentResponse.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    public ResponseEntity updatePsuInPayment(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation's identifier",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData) {

        try {
            return cmsPsuPisService.updatePsuInPayment(psuIdData, authorisationId, instanceId)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @GetMapping(path = "/redirect/{redirect-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPaymentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    public ResponseEntity getPaymentIdByRedirectId(
        @ApiParam(name = "redirect-id",
            value = "The redirect identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        Optional<CmsPaymentResponse> response;
        try {
            response = cmsPsuPisService.checkRedirectAndGetPayment(redirectId, instanceId);

            if (!response.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CmsPaymentResponse cmsPaymentResponse = response.get();

            return new ResponseEntity<>(cmsPaymentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @GetMapping(path = "/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPayment.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CmsPayment> getPaymentByPaymentId(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuPisService.getPayment(psuIdData, paymentId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping(path = "/cancellation/redirect/{redirect-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPaymentResponse.class),
        @ApiResponse(code = 404, message = "Not Found"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    public ResponseEntity getPaymentIdByRedirectIdForCancellation(
        @ApiParam(name = "redirect-id",
            value = "The redirect identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        Optional<CmsPaymentResponse> response;
        try {
            response = cmsPsuPisService.checkRedirectAndGetPaymentForCancellation(redirectId, instanceId);

            if (!response.isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CmsPaymentResponse cmsPaymentResponse = response.get();
            return new ResponseEntity<>(cmsPaymentResponse, HttpStatus.OK);
        } catch (RedirectUrlIsExpiredException e) {
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @GetMapping(path = "/cancellation/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPayment.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CmsPayment> getPaymentByPaymentIdForCancellation(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuPisService.getPayment(psuIdData, paymentId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @GetMapping(path = "authorisation/{authorisation-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPsuAuthorisation.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(
        @ApiParam(name = "authorisation-id",
            value = "The authorisation identification.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return cmsPsuPisService.getAuthorisationByAuthorisationId(authorisationId, instanceId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{payment-id}/authorisation/{authorisation-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 408, message = "Request Timeout", response = CmsPaymentResponse.class)})
    public ResponseEntity updateAuthorisationStatus(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "authorisation-id",
            value = "The payment authorisation identification assigned to the created payment authorisation.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder) {

        ScaStatus scaStatus = ScaStatus.fromValue(status);
        if (scaStatus == null) {
            return ResponseEntity.badRequest().build();
        }

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        try {
            return cmsPsuPisService.updateAuthorisationStatus(psuIdData, paymentId, authorisationId, scaStatus, instanceId, authenticationDataHolder)
                       ? ResponseEntity.ok().build()
                       : ResponseEntity.badRequest().build();
        } catch (AuthorisationIsExpiredException e) {
            return new ResponseEntity<>(new CmsPaymentResponse(e.getNokRedirectUri()), HttpStatus.REQUEST_TIMEOUT);
        }
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")})
    public ResponseEntity<Void> updatePaymentStatus(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {
        return cmsPsuPisService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status), instanceId)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/{payment-id}/authorisation/psus")
    @ApiOperation(value = "Returns list of info objects about PSU data and authorisation statuses")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPisPsuDataAuthorisation.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<CmsPisPsuDataAuthorisation>> psuAuthorisationStatuses(
        @ApiParam(name = "payment-id",
            value = "The payment identification assigned to the created payment.",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId) {

        return cmsPsuPisService.getPsuDataAuthorisations(paymentId, instanceId)
                   .map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
}
