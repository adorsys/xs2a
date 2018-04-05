package de.adorsys.aspsp.xs2a.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import static java.util.Locale.forLanguageTag;

@Slf4j
@Service
public class MessageService {
    @Value("${spring.mvc.locale}")
    private String defaultLocale;

    private MessageSource messageSource;

    @Autowired
    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code){
        try{
            return messageSource.getMessage(code, null, forLanguageTag(defaultLocale));
        }catch (Exception e){
            log.info("Can't get message: {}", e.getMessage());
        }
        return null;
    }
}
