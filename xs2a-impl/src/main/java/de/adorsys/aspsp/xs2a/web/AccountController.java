package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.service.AccountService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "api/v1/accounts")
@Api(value = "api/v1/accounts", tags = "AISP, Accounts", description = "Provides access to the Psu account")
public class AccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);
    private AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @ApiOperation(value = "Reads a list of accounts, with balances where required . It is assumed that a consent of the Psu to this access is already given and stored on the ASPSP system. The addressed list of accounts depends then on the Psu ID and the stored consent addressed by consent-id, respectively the OAuth2 token")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Map<String, List<AccountDetails>>> getAccounts(
    @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "with-balance", required = false) boolean withBalance,
    @ApiParam(name = "psu-involved", value = "If contained, it is indicated that a Psu has directly asked this account access in real-time. The Psu then might be involved in an additional consent process, if the given consent is not any more sufficient.")
    @RequestParam(name = "psu-involved", required = false) boolean psuInvolved) {
        ResponseObject<List<AccountDetails>> responseObject = accountService.getAccountDetailsList(withBalance, psuInvolved);
        HttpStatus httpStatus = responseObject.isSuccess() ? HttpStatus.OK : HttpStatus.valueOf(responseObject.getMessage().getCode());

        List<AccountDetails> accountDetailsList = responseObject.isSuccess() ? responseObject.getData() : null;
        Map<String, List<AccountDetails>> accountDetailsMap = new HashMap<>();
        accountDetailsMap.put("accountList", accountDetailsList);

        LOGGER.debug("getAccounts(): response has {} accounts", accountDetailsMap.get("accountList").size());

        return new ResponseEntity<>(responseObject.isSuccess() ? accountDetailsMap : null, httpStatus);
    }

    @ApiOperation(value = "Reads details about an account, with balances where required. It is assumed that a consent of the PSU to this access is already given and stored on the ASPSP system. The addressed details of this account depends then on the stored consent addressed by consentId, respectively the OAuth2 access token")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{account-id}", method = RequestMethod.GET)
    ResponseEntity<AccountDetails> readAccountDetails(
    @ApiParam(name = "account-id", value = "The account consent identification assigned to the created resource", example = "11111-999999999")
    @PathVariable(name = "account-id", required = true) String accountId,
    @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "with-balance", required = false) boolean withBalance,
    @ApiParam(name = "psu-involved", value = "If contained, it is indicated that a Psu has directly asked this account access in real-time. The Psu then might be involved in an additional consent process, if the given consent is not any more sufficient.")
    @RequestParam(name = "psu-involved", required = false) boolean psuInvolved) {
        ResponseObject<AccountDetails> responseObject = accountService.getAccountDetails(accountId, withBalance, psuInvolved);
        HttpStatus httpStatus = responseObject.isSuccess() ? HttpStatus.OK : HttpStatus.valueOf(responseObject.getMessage().getCode());

        LOGGER.debug("getAccount(): response is {} account-id", responseObject.isSuccess() ? responseObject.getData().getId() : responseObject.getMessage().getDescription());

        return new ResponseEntity<>(responseObject.isSuccess()
                                    ? responseObject.getData() : null, httpStatus);
    }

    @ApiOperation(value = "Read a list of the balances for the given account")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = Balances.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{account-id}/balances", method = RequestMethod.GET)
    public ResponseEntity<List<Balances>> getBalances(
    @PathVariable(name = "account-id", required = true) String accountId,
    @ApiParam(name = "psu-involved", value = "If contained, it is indicated that a Psu has directly asked this account access in realtime. The Psu then might be involved in an additional consent process, if the given consent is not any more sufficient.")
    @RequestParam(name = "psu-involved", required = false) boolean psuInvolved) {

        ResponseObject responseObject = accountService.getBalances(accountId, psuInvolved);
        HttpStatus httpStatus = (responseObject.isSuccess())
                                ? HttpStatus.OK
                                : HttpStatus.valueOf(responseObject.getMessage().getCode());

        LOGGER.debug("getBalances(): balances by account {} and psu-involved {} is {}",
        accountId, psuInvolved, responseObject.getData());

        return new ResponseEntity<>((List<Balances>) responseObject.getData(), httpStatus);
    }

    @ApiOperation(value = "Reads account data from a given account addressed by \"account-id\".")
    @ApiResponses(value = {
    @ApiResponse(code = 200, message = "OK", response = AccountReport.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{account-id}/transactions", method = RequestMethod.GET)
    public ResponseEntity<AccountReport> getTransactions(@ApiParam(name = "account-id", value = "The account consent identification assigned to the created resource")
                                                         @PathVariable(name = "account-id") String accountId,
                                                         @ApiParam(name = "date_from", value = "Starting date of the account statement", example = "2017-10-30")
                                                         @RequestParam(name = "date_from", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
                                                         @ApiParam(name = "date_to", value = "End date of the account statement", example = "2017-11-30")
                                                         @RequestParam(name = "date_to", required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo,
                                                         @ApiParam(name = "transaction_id", value = "Transaction identification", example = "1234567")
                                                         @RequestParam(name = "transaction_id", required = false) String transactionId,
                                                         @ApiParam(name = "psu-involved", value = "If contained, it is indicating that a Psu has directly asked this account access in real-time. The Psu then might be involved in an additional consent process, if the given consent is not any more sufficient.")
                                                         @RequestParam(name = "psu-involved", required = false) boolean psuInvolved,
                                                         @ApiParam(name = "bookingStatus", value = "Permited codes are 'booked', 'pending','both", example = "both")
                                                         @RequestParam(name = "bookingStatus", required = false) String bookingStatus,
                                                         @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
                                                         @RequestParam(name = "with-balance", required = false) boolean withBalance,
                                                         @ApiParam(name = "deltaList", value = "This data attribute is indicating that the AISP is in favour to get all transactions after the last report access for this PSU")
                                                         @RequestParam(name = "deltaList", required = false) boolean deltaList) {

        ResponseObject<AccountReport> accountReport = accountService.getAccountReport(accountId, dateFrom, dateTo, transactionId, psuInvolved, bookingStatus, withBalance, deltaList);
        LOGGER.debug("getTransactionsForAccount(): report for account {} date_from {} date_to {} transaction_id {} and psu-involved {} is {}"
        , accountId, dateFrom, dateTo, transactionId, psuInvolved, accountReport);
        HttpStatus httpStatus = accountReport.isSuccess() ? HttpStatus.OK : HttpStatus.valueOf(accountReport.getMessage().getCode());

        return new ResponseEntity<>(accountReport.getData(), httpStatus);
    }
}
