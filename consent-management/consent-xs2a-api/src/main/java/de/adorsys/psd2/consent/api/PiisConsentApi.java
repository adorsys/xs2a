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
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(path = "api/v1/piis/consent")
@Api(value = "api/v1/piis/consent", tags = InternalCmsXs2aApiTagName.PIIS_CONSENTS)
public interface PiisConsentApi {

    @GetMapping(path = "/{account-reference-type}/{account-identifier}")
    @ApiOperation(value = "Gets list of consents by account reference data.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    ResponseEntity<List<CmsConsent>> getPiisConsentListByAccountReference(
        @ApiParam(name = "currency", value = "Valid currency code", example = "EUR")
        @RequestHeader(value = "currency", required = false) String currency,
        @ApiParam(name = "account-reference-type",
            value = "Account reference type, can be either IBAN, BBAN, PAN, MSISDN or MASKED_PAN.",
            example = "IBAN",
            required = true)
        @PathVariable("account-reference-type") AccountReferenceType accountReferenceType,
        @ApiParam(name = "account-identifier",
            value = "The value of account identifier.",
            example = "DE2310010010123456789",
            required = true)
        @PathVariable("account-identifier") String accountIdentifier);
}
