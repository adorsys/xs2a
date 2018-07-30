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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/accounts")
@Api(value = "api/v1/accounts", tags = "AISP, Accounts", description = "Provides access to the Psu account")
public class AccountController {
    private final AccountService accountService;
    private final ResponseMapper responseMapper;

    @ApiOperation(value = "Reads a list of accounts, with balances where required . It is assumed that a consent of the Psu to this access is already given and stored on the ASPSP system. The addressed list of accounts depends then on the Psu ID and the stored consent addressed by consent-id, respectively the OAuth2 token", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Map.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Consent invalid or expired"),
        @ApiResponse(code = 429, message = "Access exceeded")})
    @GetMapping
    @ApiImplicitParams({
        @ApiImplicitParam(name = "consent-id", value = "7f53031f-3cd8-4270-b07f-4ea1456ba124", required = true, paramType = "header"),
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<Map<String, List<AccountDetails>>> getAccounts(
        @RequestHeader(name = "consent-id") String consentId,
        @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
        @RequestParam(name = "with-balance", required = false) boolean withBalance) {
        ResponseObject<Map<String, List<AccountDetails>>> responseObject = accountService.getAccountDetailsList(consentId, withBalance);
        return responseMapper.ok(responseObject);
    }

    @ApiOperation(value = "Reads details about an account, with balances where required. It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The addressed details of this account depends then on the stored consent addressed by consentId, respectively the OAuth2 access token", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AccountDetails.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Consent invalid or expired"),
        @ApiResponse(code = 404, message = "Account not found"),
        @ApiResponse(code = 429, message = "Access exceeded")})
    @GetMapping(path = "/{account-id}")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "consent-id", value = "7f53031f-3cd8-4270-b07f-4ea1456ba124", required = true, paramType = "header"),
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<AccountDetails> readAccountDetails(
        @RequestHeader(name = "consent-id") String consentId,
        @ApiParam(name = "account-id", required = true, value = "This identification is denoting the addressed account, where the transaction has been performed", example = "11111-999999999")
        @PathVariable(name = "account-id") String accountId,
        @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
        @RequestParam(name = "with-balance", required = false) boolean withBalance) {
        ResponseObject<AccountDetails> responseObject = accountService.getAccountDetails(consentId, accountId, withBalance);
        return responseMapper.ok(responseObject);
    }

    @ApiOperation(value = "Read a list of the balances for the given account", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = List.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Consent invalid or expired"),
        @ApiResponse(code = 404, message = "Account not found"),
        @ApiResponse(code = 429, message = "Access exceeded")})
    @GetMapping(path = "/{account-id}/balances")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "consent-id", value = "7f53031f-3cd8-4270-b07f-4ea1456ba124", required = true, paramType = "header"),
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", dataType = "String", paramType = "header")})
    public ResponseEntity<List<Balance>> getBalances(
        @RequestHeader(name = "consent-id") String consentId,
        @ApiParam(name = "account-id", required = true, value = "This identification is denoting the addressed account, where the transaction has been performed")
        @PathVariable(name = "account-id") String accountId) {
        ResponseObject<List<Balance>> responseObject = accountService.getBalances(consentId, accountId);
        return responseMapper.ok(responseObject);
    }

    @ApiOperation(value = "Reads account data from a given account addressed by \"account-id\".", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AccountReport.class),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 401, message = "Consent invalid or expired"),
        @ApiResponse(code = 429, message = "Access exceeded")})
    @GetMapping(path = "/{account-id}/transactions")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "consent-id", value = "7f53031f-3cd8-4270-b07f-4ea1456ba124", required = true, paramType = "header"),
        @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "x-request-id", value = "2f77a125-aa7a-45c0-b414-cea25a116035", required = true, dataType = "UUID", paramType = "header"),
        @ApiImplicitParam(name = "timestamp", value = "Sun, 06 Aug 2017 15:02:37 GMT", required = false, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "digest", value = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6", dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "signature", value = "98c0", required = false, dataType = "String", paramType = "header"),
        @ApiImplicitParam(name = "tpp-signature-certificate", value = "some certificate", required = false, dataType = "String", paramType = "header")})
    public ResponseEntity<AccountReport> getTransactions(@ApiParam(name = "account-id", required = true, value = "The account consent identification assigned to the created resource")
                                                         @PathVariable(name = "account-id") String accountId,
                                                         @RequestHeader(name = "consent-id", required = false) String consentId,
                                                         @ApiParam(name = "dateFrom", value = "Starting date of the account statement", example = "2017-10-30")
                                                         @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                                         @ApiParam(name = "dateTo", value = "End date of the account statement", example = "2017-11-30")
                                                         @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                                         @ApiParam(name = "transactionId", value = "Transaction identification", example = "1234567")
                                                         @RequestParam(name = "transactionId", required = false) String transactionId,
                                                         @ApiParam(name = "psuInvolved", value = "If contained, it is indicating that a Psu has directly asked this account access in real-time. The Psu then might be involved in an additional consent process, if the given consent is not any more sufficient.")
                                                         @RequestParam(name = "psuInvolved", required = false) boolean psuInvolved,
                                                         @ApiParam(name = "bookingStatus", example = "both", required = true, allowableValues = "booked, pending, both")
                                                         @RequestParam(name = "bookingStatus") String bookingStatus,
                                                         @ApiParam(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
                                                         @RequestParam(name = "withBalance", required = false) boolean withBalance,
                                                         @ApiParam(name = "deltaList", value = "This data attribute is indicating that the AISP is in favour to get all transactions after the last report access for this PSU")
                                                         @RequestParam(name = "deltaList", required = false) boolean deltaList) {
        ResponseObject<AccountReport> responseObject =
            accountService.getAccountReport(consentId, accountId, dateFrom, dateTo, transactionId, psuInvolved, BookingStatus.forValue(bookingStatus), withBalance, deltaList);
        return responseMapper.ok(responseObject);
    }
}
