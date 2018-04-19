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

    @GetMapping(path = "/allConsents")
    public ResponseEntity<List<SpiAccountConsent>> readAllConsents() {
        return ResponseEntity.ok(consentService.getAllConsents());
    }

    @GetMapping(path = "/")
    public ResponseEntity<SpiAccountConsent> readConsentById(@RequestParam(name = "consentId") String id) {
        SpiAccountConsent consent = consentService.getConsent(id);
        return consent == null
               ? new ResponseEntity<>(HttpStatus.NOT_FOUND)
               : new ResponseEntity<>(consent, HttpStatus.OK);
    }

    @PostMapping(path = "/")
    public ResponseEntity<String> createConsent(@RequestBody SpiCreateConsentRequest requestConsent, @RequestParam(required = false) String psuId) {
        String saved = consentService.createConsentAndReturnId(requestConsent, psuId);
        return saved == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST) : new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deleteConsent(@PathVariable("id") String id) {
        if (consentService.deleteConsentById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
