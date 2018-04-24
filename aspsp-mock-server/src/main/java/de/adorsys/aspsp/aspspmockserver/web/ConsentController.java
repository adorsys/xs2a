package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/consent")
public class ConsentController {
    private ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountConsent>> readAllConsents() {
        return ResponseEntity.ok(consentService.getAllConsents());
    }

    @GetMapping(path = "/{consent-id}")
    public ResponseEntity<SpiAccountConsent> readConsentById(@PathVariable("consent-id") String consentId) {
        SpiAccountConsent consent = consentService.getConsent(consentId);
        return consent == null
                       ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
                       : new ResponseEntity<>(consent, HttpStatus.OK);
    }

    @PostMapping(path = "/")
    public ResponseEntity<String> createConsent(@RequestBody SpiCreateConsentRequest requestConsent, @RequestParam(required = false) String psuId) {
        return consentService.createConsentAndReturnId(requestConsent, psuId)
                       .map(consentId -> new ResponseEntity<>(consentId, HttpStatus.CREATED))
                       .orElse(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping(path = "/{consent-id}")
    public ResponseEntity deleteConsent(@PathVariable("consent-id") String consentId) {
        if (consentService.deleteConsentById(consentId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
