package de.adorsys.aspsp.xs2a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class ASPSPXs2aApplication {

    public static void main(String[] args) {
        SpringApplication.run(ASPSPXs2aApplication.class, args);
    }
}
