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

import de.adorsys.psd2.consent.api.ais.UpdateTransactionParametersRequest;
import de.adorsys.psd2.consent.api.config.InternalCmsXs2aApiTagName;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "api/v1/ais/consent")
@Api(value = "api/v1/ais/consent", tags = InternalCmsXs2aApiTagName.AIS_CONSENTS)
public interface CmsAccountApi {

    @PutMapping(path = "/{encrypted-consent-id}/{resource-id}")
    @ApiOperation(value = "Saves number of transactions for a definite account of the definite consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<Boolean> saveTransactionParameters(
        @ApiParam(name = "consent-id",
            value = "Encrypted consent ID",
            example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7",
            required = true)
        @PathVariable("encrypted-consent-id") String encryptedConsentId,
        @PathVariable("resource-id") String resourceId,
        @RequestBody UpdateTransactionParametersRequest updateTransactionParametersRequest);
}
