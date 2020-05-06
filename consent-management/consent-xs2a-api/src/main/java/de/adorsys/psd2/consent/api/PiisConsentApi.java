/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
