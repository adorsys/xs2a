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

package de.adorsys.psd2.consent.web.psu;

import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.psu.api.CmsPsuPisService;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/psu/pis/consent")
@Api(value = "api/v1/psu/pis/consent", tags = "PSU PIS, Consents", description = "Test controller for cms-psu-api providing access for PIS consents")
public class CmsPsuPisController {
    private final CmsPsuPisService cmsPsuPisService;

    @PutMapping(path = "/{payment-id}")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CreatePisConsentResponse.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CreatePisConsentResponse> updatePsuInPayment(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuPisService.updatePsuInPayment(psuIdData, paymentId)
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/{payment-id}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CmsPayment.class),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<CmsPayment> getPaymentByPaymentId(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId) {

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuPisService.getPayment(psuIdData, paymentId)
                   .map(payment -> new ResponseEntity<>(payment, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "/{payment-id}/{authorisation-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 400, message = "Bad request")})
    public ResponseEntity<Void> updateAuthorisationStatus(
        @ApiParam(value = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceeding AIS service in the same session. ")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @ApiParam(value = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @ApiParam(value = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(name = "authorisation-id", value = "The payment authorisation identification assigned to the created payment authorisation.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(value = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted'. These values might be extended by ASPSP by more values.", allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED")
        @PathVariable("status") String status) {

        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);
        return cmsPsuPisService.updateAuthorisationStatus(psuIdData, paymentId, authorisationId, ScaStatus.valueOf(status))
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.badRequest().build();
    }

    @PutMapping(path = "/{payment-id}/status/{status}")
    @ApiOperation(value = "")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not found")})
    public ResponseEntity<Void> updatePaymentStatus(
        @ApiParam(name = "payment-id", value = "The payment identification assigned to the created payment.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("payment-id") String paymentId,
        @ApiParam(value = "The following code values are permitted 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'PDNG', 'RCVD', 'RJCT', 'CANC'. These values might be extended by ASPSP by more values.", allowableValues = "ACCP,  ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC")
        @PathVariable("status") String status) {
        return cmsPsuPisService.updatePaymentStatus(paymentId, TransactionStatus.valueOf(status))
                   ? ResponseEntity.ok().build()
                   : ResponseEntity.notFound().build();
    }
}
