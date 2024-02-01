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

package de.adorsys.psd2.consent.api;

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "api/v1/ais/consent")
@Tag(name = InternalCmsXs2aApiTagName.AIS_CONSENTS_TRANSACTIONS, description = "Provides an ability to update transactions for the consent's account")
public interface CmsAccountApi {

    @PutMapping(path = "/{encrypted-consent-id}/{resource-id}")
    @Operation(description = "Saves number of transactions for a definite account of the definite consent")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<Boolean> saveTransactionParameters(
        @Parameter(name = "consent-id",
            description = "Encrypted consent ID",
            example = "8DdEDIu4UwogNXxJ_odI5vB6yMy3O50XYi27ZxLOSUxDUmWboSIeVIu1BJdujlNZosjUHBJ3bceNNtFShb3nvMz9MpaJIQIH3NJX8IHgetw=_=_psGLvQpt9Q",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @PathVariable("resource-id") String resourceId,
        @RequestBody UpdateTransactionParametersRequest updateTransactionParametersRequest);
}
