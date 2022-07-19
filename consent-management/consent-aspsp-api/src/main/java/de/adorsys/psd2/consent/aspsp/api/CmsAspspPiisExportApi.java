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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import de.adorsys.psd2.consent.api.ResponseData;
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;

@RequestMapping(path = "aspsp-api/v1/piis/consents")
@Tag(name = CmsAspspApiTagName.ASPSP_PIIS_CONSENTS_EXPORT, description = "Provides access to the consent management system for exporting PIIS consents by ASPSP")
public interface CmsAspspPiisExportApi {

    @GetMapping(path = "/tpp/{tpp-id}")
    @Operation(description = "Returns a list of consents by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseData<Collection<CmsPiisConsent>> getConsentsByTpp(
        @Parameter(description = "TPP ID", required = true, example = "12345987")
        @PathVariable("tpp-id") String tppId,
        @Parameter(description = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @Parameter(description = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's" +
            " documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/psu")
    @Operation(description = "Returns a list of consents by given mandatory PSU ID Data, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseData<Collection<CmsPiisConsent>> getConsentsByPsu(
        @Parameter(description = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @Parameter(description = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @Parameter(description = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session.")
        @RequestHeader(value = "psu-id", required = false) String psuId,
        @Parameter(description = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility")
        @RequestHeader(value = "psu-id-type", required = false) String psuIdType,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id", required = false) String psuCorporateId,
        @Parameter(description = "Might be mandated in the ASPSP's documentation. Only used in a corporate context.")
        @RequestHeader(value = "psu-corporate-id-type", required = false) String psuCorporateIdType,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);

    @GetMapping(path = "/account/{account-id}")
    @Operation(description = "Returns a list of consents by given mandatory ASPSP account ID, optional creation date and instance ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK")})
    ResponseData<Collection<CmsPiisConsent>> getConsentsByAccountId(
        @Parameter(description = "Bank specific account identifier", required = true, example = "11111-99999")
        @PathVariable("account-id") String aspspAccountId,
        @Parameter(description = "Creation start date", example = "2010-01-01")
        @RequestHeader(value = "start-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @Parameter(description = "Creation end date", example = "2030-01-01")
        @RequestHeader(value = "end-date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @Parameter(description = "ID of the particular service instance")
        @RequestHeader(value = "instance-id", required = false) String instanceId,
        @Parameter(description = "Index of current page", example = "0")
        @RequestParam(value = CmsConstant.QUERY.PAGE_INDEX, defaultValue = "0") Integer pageIndex,
        @Parameter(description = "Quantity of consents on one page", example = "20")
        @RequestParam(value = CmsConstant.QUERY.ITEMS_PER_PAGE, defaultValue = "20") Integer itemsPerPage);
}
