package de.adorsys.aspsp.xs2a.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import static java.util.Locale.forLanguageTag;

@Slf4j
@Service
@AllArgsConstructor
public class MessageService {
    private final MessageSource messageSource;

    public String getMessage(String code){
        try{
            return messageSource.getMessage(code, null, forLanguageTag("en"));
        }catch (Exception e){
            log.info("Can't get message: {}", e.getMessage());
        }
        return null;
    }
}
