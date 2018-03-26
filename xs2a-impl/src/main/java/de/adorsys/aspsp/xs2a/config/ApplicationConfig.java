package de.adorsys.aspsp.xs2a.config;

import de.adorsys.aspsp.xs2a.spi.impl.AccountSpiImpl;
import de.adorsys.aspsp.xs2a.spi.impl.ConsentSpiImpl;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public AccountSpi accountSpi() {
        return new AccountSpiImpl();
    }

    @Bean
    public ConsentSpi consentSpi() {
        return new ConsentSpiImpl();
    }
}
