package de.adorsys.aspsp.xs2a.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.service.validator.RequestValidatorService;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.web.interceptor.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Validation;
import javax.validation.Validator;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {
    @Value("${application.ais.transaction.max-length}")
    private int maxNumberOfCharInTransactionJson;

    @Value("${application.ais.consents.link.redirect-to}")
    private String consentsLinkRedirectToSource;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:error_message");
        messageSource.setCacheSeconds(3600); //reload messages every hour
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModule(new Jdk8Module()); // add support for Optionals
        objectMapper.registerModule(new JavaTimeModule()); // add support for java.time types
        objectMapper.registerModule(new ParameterNamesModule()); // support for multiargs constructors
        return objectMapper;
    }

    @Bean
    public ParametersFactory parametersFactory(){
        return new ParametersFactory(objectMapper());
    }

    @Bean
    public RequestValidatorService requestValidatorService() {
        return new RequestValidatorService(validator(), parametersFactory());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor(requestValidatorService()));
    }

    @Bean
    public int maxNumberOfCharInTransactionJson() {
        return maxNumberOfCharInTransactionJson;
    }

    @Bean
    public String consentsLinkRedirectToSource() {
        return consentsLinkRedirectToSource;
    }

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
