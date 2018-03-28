package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentResp;
import de.adorsys.aspsp.xs2a.service.ConsentService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(path = "api/v1/consents")
@Api(value = "api/v1/consents", tags = "AISP Consents", description = "Provides access to the Psu Consents")
public class ConsentInformationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsentInformationController.class);
    private ConsentService consentService;

    @Autowired
    public ConsentInformationController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "OK", response = CreateConsentResp.class), @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<CreateConsentResp> createAccountConsent(
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,
    @ApiParam(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "withBalance", required = false) boolean withBalance,
    @Valid @RequestBody CreateConsentReq createConsent) {
        CreateConsentResp aicCreateResponse = consentService.createAccountConsentsWithResponse(createConsent, withBalance, tppRedirectPreferred);

        LOGGER.debug("createAccountConsent(): response {} ", aicCreateResponse);

        return new ResponseEntity<>(aicCreateResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Can check the status of an account information consent resource")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Map.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consent-id}/status", method = RequestMethod.GET)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<Map<String, TransactionStatus>> getAccountConsentsStatusById(
    @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource")
    @PathVariable("consent-id") String consentId) {

        Map<String, TransactionStatus> accountConsentsStatusResponse = new HashMap<>();
        TransactionStatus transactionStatus = consentService.getAccountConsentsStatusById(consentId);
        accountConsentsStatusResponse.put("transactionStatus", transactionStatus);

        LOGGER.debug("getAccountConsentStatusById(): response {} ", transactionStatus);

        return new ResponseEntity<>(accountConsentsStatusResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Returns the content of an account information consent object")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AccountConsent.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consent-id}", method = RequestMethod.GET)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<AccountConsent> getAccountConsentsInformationById(
    @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created resource")
    @PathVariable("consent-id") String consentId) {
        AccountConsent accountConsent = consentService.getAccountConsentsById(consentId);

        LOGGER.debug("getAccountConsentsInformationById(): response {} ", accountConsent);

        return new ResponseEntity<>(accountConsent, HttpStatus.OK);
    }

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP to return a list of all accessible accounts",
    notes = "if withBalance is true then the balance is on the list off all payments accounts ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AccountDetails[].class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/account-list", method = RequestMethod.POST)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public Resource<List<AccountDetails>> createAICResource(
    @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "with-balance", required = true) Boolean withBalance) {

        Link link = linkTo(ConsentInformationController.class).withSelfRel();
        return new Resource<>(getAllAccounts(withBalance), link);
    }

    @ApiOperation(value = " Delete information consent object")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consent-id}", method = RequestMethod.DELETE)
    @ApiImplicitParams({
    @ApiImplicitParam(name = "tpp-transaction-id", value = "16d40f49-a110-4344-a949-f99828ae13c9", required = true, dataType = "UUID", paramType = "header"),
    @ApiImplicitParam(name = "tpp-request-id", value = "21d40f65-a150-8343-b539-b9a822ae98c0", required = true, dataType = "UUID", paramType = "header")})
    public ResponseEntity<Void> deleteAccountConsent(
    @ApiParam(name = "consent-id", value = "The resource-id of consent to be deleted")
    @PathVariable("consent-id") String consentId) {
        consentService.deleteAccountConsentsById(consentId);
        LOGGER.debug("deleteAccountConsent(): deleted according to id {} ", consentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private List<AccountDetails> getAllAccounts(Boolean withBalance) {
        // TODO according task AIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/11
        return new ArrayList<>();
    }
}
