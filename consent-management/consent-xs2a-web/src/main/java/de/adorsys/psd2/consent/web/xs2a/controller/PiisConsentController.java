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

package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/piis/consent")
@Api(value = "api/v1/piis/consent", tags = "PIIS, Consents", description = "Provides access to consent management system for PIIS")
public class PiisConsentController {
    private final PiisConsentService piisConsentService;

    @GetMapping(path = "/{currency}/{account-reference-type}/{account-identifier}")
    @ApiOperation(value = "Gets list of consents by account reference data.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<PiisConsent>> getPiisConsentListByAccountReference(
        @ApiParam(name = "currency", value = "3 capital letters of currency name.", example = "EUR")
        @PathVariable("currency") String currency,
        @ApiParam(name = "account-reference-type", value = "Account reference type, can be either IBAN, BBAN, PAN, MSISDN or MASKED_PAN.", example = "IBAN")
        @PathVariable("account-reference-type") AccountReferenceType accountReferenceType,
        @ApiParam(name = "account-identifier", value = "The value of account identifier.", example = "DE2310010010123456789")
        @PathVariable("account-identifier") String accountIdentifier) {
        List<PiisConsent> responseList = piisConsentService.getPiisConsentListByAccountIdentifier(Currency.getInstance(currency), new AccountReferenceSelector(accountReferenceType, accountIdentifier));
        return CollectionUtils.isEmpty(responseList)
                   ? ResponseEntity.notFound().build()
                   : ResponseEntity.ok(responseList);
    }
}
