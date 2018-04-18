package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.PsuService;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/psu")
public class PsuController {
    private PsuService psuService;

    public PsuController(PsuService psuService) {
        this.psuService = psuService;
    }

    @GetMapping(path = "/")
    public ResponseEntity<List<Psu>> readAllPsus() {
        return ResponseEntity.ok(psuService.getAllPsus());
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Psu> readPsuById(@PathVariable("id") String id) {
        return psuService.getPsu(id)
               .map(ResponseEntity::ok)
               .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/")
    public ResponseEntity createPsu(HttpServletRequest request,
                                    @RequestBody List<SpiAccountDetails> accountDetailsList) throws Exception {
        String uriString = getUriString(request);
        String saved = psuService.createPsuAndReturnId(accountDetailsList);
        System.out.println(saved);
        return ResponseEntity.created(new URI(uriString + saved)).build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity deletePsu(@PathVariable("id") String id) {
        if (psuService.deletePsuById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
