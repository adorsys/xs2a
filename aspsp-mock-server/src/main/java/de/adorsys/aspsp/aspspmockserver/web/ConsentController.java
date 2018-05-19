package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/consent")
public class ConsentController {
    private final ConsentService consentService;

    @GetMapping(path = "/")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<List<SpiAccountConsent>> readAllConsents() {
        return ResponseEntity.ok(consentService.getAllConsents());
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<SpiAccountConsent> readConsentById(@PathVariable("consent-id") String consentId) {
        Optional<SpiAccountConsent> consent = consentService.getConsent(consentId);
        return consent
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity<String> createConsent(@RequestBody SpiAccountConsent consent) {
        Optional<String> consentId = consentService.createConsentAndReturnId(consent);

        return consentId.map(id -> new ResponseEntity<>(id, HttpStatus.CREATED))
                   .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping(path = "/{consent-id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "read", description = "Access read API")})})
    public ResponseEntity deleteConsent(@PathVariable("consent-id") String consentId) {
        if (consentService.deleteConsentById(consentId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
