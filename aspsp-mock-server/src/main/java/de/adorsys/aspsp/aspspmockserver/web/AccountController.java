package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.AccountService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/account")
public class AccountController {
    private AccountService accountService;

    public AccountController(AccountService accountService) {this.accountService = accountService;}

    @GetMapping(path = "/")
    public ResponseEntity<List<SpiAccountDetails>> readAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping(path = "/{id}/")
    public ResponseEntity<SpiAccountDetails> readAccountById(@PathVariable("id") String id) {
        return accountService.getAccount(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(path = "/")
    public ResponseEntity createAccount(HttpServletRequest request,
                                    @RequestBody SpiAccountDetails account) throws Exception {
        String uriString = getUriString(request);
        SpiAccountDetails saved = accountService.addAccount(account);
        return ResponseEntity.created(new URI(uriString + saved.getId())).build();
    }

    private String getUriString(HttpServletRequest request) {
        return UriComponentsBuilder.fromHttpRequest(new ServletServerHttpRequest(request)).build().toUriString();
    }
}
