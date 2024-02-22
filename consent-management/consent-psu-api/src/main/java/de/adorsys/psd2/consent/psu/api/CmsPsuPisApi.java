/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsPaymentResponse;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.psu.api.config.CmsPsuApiTagName;
import de.adorsys.psd2.consent.psu.api.pis.CmsPisPsuDataAuthorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "psu-api/v1/payment")
@Tag(name = CmsPsuApiTagName.PSU_PIS_PAYMENT, description = "CMS-PSU PIS Controller")
public interface CmsPsuPisApi {

    @PutMapping(path = "/authorisation/{authorisation-id}/psu-data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CreatePisCommonPaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class)))})
    ResponseEntity<Object> updatePsuInPayment(
        @Parameter(name = "authorisation-id",
            description = "The authorisation's identifier",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody PsuIdData psuIdData);

    @PutMapping(path = "/{payment-service}/{payment-product}/{payment-id}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "408", description = "Request Timeout")})
    ResponseEntity<Object> updatePayment(

        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,

        @Schema(description = "Payment service. Permitted values are : payments, bulk-payments, periodic-payments",
            allowableValues = "payments, bulk-payments, periodic-payments",
            example = "payments",
            required = true)
        @PathVariable("payment-service") String paymentService,

        @Parameter(description = "Payment product",
            example = "sepa-credit-transfers",
            required = true)
        @PathVariable("payment-product") String paymentProduct,

        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody Object body);

    @GetMapping(path = "/redirect/{redirect-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class)))})
    ResponseEntity<Object> getPaymentIdByRedirectId(
        @Parameter(name = "redirect-id",
            description = "The redirect identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/{payment-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsBasePaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<CmsBasePaymentResponse> getPaymentByPaymentId(
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility.")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/cancellation/redirect/{redirect-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class)))})
    ResponseEntity<Object> getPaymentIdByRedirectIdForCancellation(
        @Parameter(name = "redirect-id",
            description = "The redirect identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("redirect-id") String redirectId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/cancellation/{payment-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsBasePaymentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<CmsBasePaymentResponse> getPaymentByPaymentIdForCancellation(
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility.")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(name = "payment-id",
            description = "The payment identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "authorisation/{authorisation-id}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsPsuAuthorisation.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")})
    ResponseEntity<CmsPsuAuthorisation> getAuthorisationByAuthorisationId(
        @Parameter(name = "authorisation-id",
            description = "The authorisation identification",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @PutMapping(path = "/{payment-id}/authorisation/{authorisation-id}/status/{status}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad request"),
        @ApiResponse(responseCode = "408", description = "Request Timeout", content = @Content(schema = @Schema(implementation = CmsPaymentResponse.class)))})
    ResponseEntity<Object> updateAuthorisationStatus(
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility.")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Parameter(name = "authorisation-id",
            description = "The payment authorisation identification assigned to the created payment authorisation",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("authorisation-id") String authorisationId,
        @Schema(description = "The following code values are permitted 'received', 'psuIdentified', 'psuAuthenticated', 'scaMethodSelected', 'started', 'finalised', 'failed', 'exempted', 'unconfirmed'. These values might be extended by ASPSP by more values.",
            allowableValues = "RECEIVED, PSUIDENTIFIED, PSUAUTHENTICATED, SCAMETHODSELECTED,  STARTED,  FINALISED, FAILED, EXEMPTED, UNCONFIRMED",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestBody(required = false) AuthenticationDataHolder authenticationDataHolder);

    @PutMapping(path = "/{payment-id}/status/{status}")
    @Operation(description = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")})
    ResponseEntity<Void> updatePaymentStatus(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @Schema(description = "The following code values are permitted 'ACCC', 'ACCP', 'ACSC', 'ACSP', 'ACTC', 'ACWC', 'ACWP', 'PDNG', 'RJCT', 'RCVD', 'CANC', 'ACFC', 'PATC'. These values might be extended by ASPSP by more values.",
            allowableValues = "ACCC, ACCP, ACSC, ACSP, ACTC, ACWC, ACWP, RCVD, PDNG, RJCT, CANC, ACFC, PATC",
            required = true)
        @PathVariable("status") String status,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);

    @GetMapping(path = "/{payment-id}/authorisation/psus")
    @Operation(description = "Returns list of info objects about PSU data and authorisation statuses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CmsPisPsuDataAuthorisation.class))),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<List<CmsPisPsuDataAuthorisation>> psuAuthorisationStatuses(
        @Parameter(name = "payment-id",
            description = "The payment identification assigned to the created payment",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("payment-id") String paymentId,
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId,
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, required = false) Integer pageIndex,
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, required = false) Integer itemsPerPage);
}
