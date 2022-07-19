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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(path = "api/v1/piis/consent")
@Tag(name = InternalCmsXs2aApiTagName.PIIS_CONSENTS, description = "Provides access to consent management system for PIIS")
public interface PiisConsentApi {

    @GetMapping(path = "/{account-reference-type}/{account-identifier}")
    @Operation(description = "Gets list of consents by account reference data.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<List<CmsConsent>> getPiisConsentListByAccountReference(
        @Parameter(name = "currency", description = "Valid currency code", example = "EUR")
        @RequestHeader(value = "currency", required = false) String currency,
        @Parameter(name = "account-reference-type",
            description = "Account reference type, can be either IBAN, BBAN, PAN, MSISDN or MASKED_PAN",
            example = "IBAN",
            required = true)
        @PathVariable("account-reference-type") AccountReferenceType accountReferenceType,
        @Parameter(name = "account-identifier",
            description = "The value of account identifier",
            example = "DE2310010010123456789",
            required = true)
        @PathVariable("account-identifier") String accountIdentifier);
}
