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

package de.adorsys.psd2.consent.aspsp.api;

import de.adorsys.psd2.consent.aspsp.api.config.CmsAspspApiTagName;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import static de.adorsys.psd2.consent.aspsp.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;

@RequestMapping(path = "aspsp-api/v1/tpp")
@Tag(name = CmsAspspApiTagName.ASPSP_TPP_INFO, description = "Provides access to the consent management system TPP Info")
public interface CmsAspspTppInfoApi {

    @GetMapping
    @Operation(description = "Returns TPP info by TPP ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not Found")})
    ResponseEntity<TppInfo> getTppInfo(
        @Parameter(description = "ID of TPP", required = true, example = "12345987")
        @RequestHeader(value = "tpp-authorisation-number") String tppAuthorisationNumber,
        @Parameter(description = "Service instance ID", example = "instance id")
        @RequestHeader(value = "instance-id", required = false, defaultValue = DEFAULT_SERVICE_INSTANCE_ID) String instanceId);
}
