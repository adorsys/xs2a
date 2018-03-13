package de.adorsys.aspsp.xs2a.config;

import de.adorsys.aspsp.xs2a.spi.impl.AccountSpiImpl;
import de.adorsys.aspsp.xs2a.spi.impl.ConsentSpiImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Value("${application.ais.transaction.max-length}")
    private int maxLengthTransactionJson;
    
    @Value("${application.ais.consents.link.redirect-to}")
    private String redirectToLink;
    
    @Bean
    public int maxLengthTransactionJson() {
        return maxLengthTransactionJson;
    }
    
    @Bean
    public String redirectToLink() {
        return redirectToLink;
    }
    
    @Bean
    public AccountSpi accountSpi() {
        return new AccountSpiImpl();
    }
    
    @Bean
    public ConsentSpi consentSpi() {
        return new ConsentSpiImpl();
    }
}
