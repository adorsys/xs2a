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
    private int maxNumberOfCharInTransactionJson;

    @Value("${application.ais.consents.link.redirect-to}")
    private String consentsLinkRedirectToSource;

    @Bean
    public int maxNumberOfCharInTransactionJson() {
        return maxNumberOfCharInTransactionJson;
    }

    @Bean
    public String consentsLinkRedirectToSource() {
        return consentsLinkRedirectToSource;
    }
}
