package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.exception.MessageError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Currency;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.FORMAT_ERROR;
import static de.adorsys.aspsp.xs2a.exception.MessageCategory.ERROR;

@Service
public class ExceptionService {
    private MessageService messageService;

    @Autowired
    public ExceptionService(MessageService messageService) {
        this.messageService = messageService;
    }

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
