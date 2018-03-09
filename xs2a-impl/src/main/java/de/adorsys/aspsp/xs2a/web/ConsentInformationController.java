package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.ais.AisStatusResponseBody;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentRequestBody;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AccountInformationConsentResponseBody;
import de.adorsys.aspsp.xs2a.spi.domain.delete_extra_class.AisAccountsList;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(path = "api/v1/consents")
@Api(value = "api/v1/consents", tags = "AISP Consents", description = "Provides access to the Psu Consents")
public class ConsentInformationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentInitiationController.class);

    @Autowired
    private ConsentService consentService;

    @ApiOperation(value = "Creates an account information consent resource at the ASPSP regarding access to accounts specified in this request.")
    @ApiResponses(value = {@ApiResponse(code = 201, message = "OK", response = AccountInformationConsentResponseBody.class), @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AccountInformationConsentResponseBody> createConsentForAccounts(
    @ApiParam(name = "tppRedirectPreferred", value = "If it equals “true”, the TPP prefers a redirect over an embedded SCA approach.")
    @RequestParam(name = "tppRedirectPreferred", required = false) boolean tppRedirectPreferred,

    @ApiParam(name = "withBalance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "withBalance", required = false) boolean withBalance,
    @RequestBody AccountInformationConsentRequestBody aicRequestBody) {
        AccountInformationConsentResponseBody aicResponse = consentService.createAicRequest(aicRequestBody, withBalance, tppRedirectPreferred);

        return new ResponseEntity<AccountInformationConsentResponseBody>(aicResponse, HttpStatus.OK);
    }

    @ApiOperation(value = "Creats an account information consent resource at the ASPSP to return a list of all accessible accounts",
    notes = "if withBalance is true then the balance is on the list off all payments accounts ")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Account[].class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/account-list", method = RequestMethod.POST)
    public Resource<List<Account>> createAICRessource(
    @ApiParam(name = "with-balance", value = "If contained, this function reads the list of accessible payment accounts including the balance.")
    @RequestParam(name = "with-balance", required = true) Boolean withBalance) {

        Link link = linkTo(ConsentInformationController.class).withSelfRel();
        return new Resource<>(getAllAccounts(withBalance), link);

    }

    @ApiOperation(value = "Check the status of an account information consent resource")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AisStatusResponseBody.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consentID}/status", method = RequestMethod.GET)
    public ResponseEntity<AisStatusResponseBody> getStatusForAIC(@PathVariable("consentID") String consentID) {

        return new ResponseEntity<AisStatusResponseBody>(getTransactionStatus(consentID), HttpStatus.OK);
    }

    @ApiOperation(value = " Returns the content of an account information consent object")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = AisAccountsList.class),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consentID}", method = RequestMethod.GET)
    public ResponseEntity<AisAccountsList> getInformationsForAIC(@PathVariable("consentID") String consentID) {
        return new ResponseEntity<AisAccountsList>(getAccountsList(consentID), HttpStatus.OK);
    }

    @ApiOperation(value = " Delete information consent object")
    @ApiResponses(value = {@ApiResponse(code = 204, message = "No Content"),
    @ApiResponse(code = 400, message = "Bad request")})
    @RequestMapping(value = "/{consentID}", method = RequestMethod.DELETE)
    public HttpEntity<Void> deleteAIC(@PathVariable String consentID) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    private AisAccountsList getAccountsList(String consentID) {
        // TODO according task AIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/11
        return null;
    }

    private AisStatusResponseBody getTransactionStatus(String consentID) {
        // TODO according task AIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/11
        return null;
    }

    private List<Account> getAllAccounts(Boolean withBalance) {
        // TODO according task AIS_01_01. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/11
        return new ArrayList<Account>();
    }
}
