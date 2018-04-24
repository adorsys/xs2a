package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.FutureBookingsService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping(path = "/future-bookings")
public class FutureBookingsController {
    private final FutureBookingsService futureBookingsService;

    @Autowired
    public FutureBookingsController(FutureBookingsService futureBookingsService) {
        this.futureBookingsService = futureBookingsService;
    }

    @PostMapping(path = "/{accountId}")
    public ResponseEntity<SpiAccountDetails> changeBalances(
        @PathVariable("accountId") String accountId) throws Exception {
        return futureBookingsService.changeBalances(accountId)
                   .map(saved -> new ResponseEntity<>(saved, OK))
                   .orElse(ResponseEntity.notFound().build());
    }
}
