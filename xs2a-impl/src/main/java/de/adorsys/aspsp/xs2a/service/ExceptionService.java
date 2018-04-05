package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.FORMAT_ERROR;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Service
@AllArgsConstructor
public class ExceptionService {
    private final MessageService messageService;

    public ResponseObject<Amount> getAmount(boolean exc) {
        if (exc) {
            return new ResponseObject<>(new MessageError(new TppMessageInformation(ERROR, FORMAT_ERROR)
                                                         .text(messageService.getMessage(FORMAT_ERROR.name()))));
        }
        Amount amount = new Amount();
        amount.setContent("Some content");
        amount.setCurrency(Currency.getInstance("EUR"));
        return new ResponseObject<>(amount);
    }
}
