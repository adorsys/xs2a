package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.service.ExceptionService;
import de.adorsys.aspsp.xs2a.service.ResponseMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/exception")
public class TestExceptionController {
    private ResponseMapper responseMapper;
    private ExceptionService exceptionService;

    @GetMapping
    public ResponseEntity<Amount> testException(@RequestParam(name = "exc") boolean withException){
        return responseMapper.ok(exceptionService.getAmount(withException));
    }
}
