/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import de.adorsys.psd2.core.data.AccountAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "api/v1/ais/consent")
@Tag(name = InternalCmsXs2aApiTagName.AIS_CONSENTS, description = "Provides access to consent management system for AIS")
public interface AisConsentApi {

    @PostMapping(path = "/action")
    @Operation(description = "Save information about uses of consent")
    ResponseEntity<Object> saveConsentActionLog(@RequestBody AisConsentActionRequest request);

    @PutMapping(path = "/{encrypted-consent-id}/access")
    @Operation(description = "Update AccountAccess in the consent identified by given consent ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Checksum verification failed"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Object> updateAccountAccess(
        @Parameter(name = "consent-id",
            description = "The account consent identification assigned to the created account consent",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @RequestBody AccountAccess request);
}
