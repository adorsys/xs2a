package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent")
public class ConsentController {
    private final ConsentService consentService;

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountConsent>> readAllConsents() {
        return ResponseEntity.ok(consentService.getAllConsents());
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @GetMapping(path = "/{consent-id}")
    public ResponseEntity<SpiAccountConsent> readConsentById(@PathVariable("consent-id") String consentId) {
        SpiAccountConsent consent = consentService.getConsent(consentId);
        return consent == null
            ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
            : new ResponseEntity<>(consent, HttpStatus.OK);
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @PostMapping(path = "/")
    public ResponseEntity<String> createConsent(@RequestBody SpiCreateConsentRequest requestConsent,
                                                @RequestParam(required = false) String psuId,
                                                @RequestParam(required = false) boolean withBalance,
                                                @RequestParam(required = false) boolean tppRedirectPreferred) {
        return consentService.createConsentAndReturnId(requestConsent, psuId, withBalance)
            .map(consentId -> new ResponseEntity<>(consentId, HttpStatus.CREATED))
            .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    @DeleteMapping(path = "/{consent-id}")
    public ResponseEntity deleteConsent(@PathVariable("consent-id") String consentId) {
        if (consentService.deleteConsentById(consentId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
