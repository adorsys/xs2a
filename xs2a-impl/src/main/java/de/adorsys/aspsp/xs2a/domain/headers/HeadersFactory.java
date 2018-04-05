package de.adorsys.aspsp.xs2a.domain.headers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.headers.impl.*;
import de.adorsys.aspsp.xs2a.web.AccountController;
import de.adorsys.aspsp.xs2a.web.FundsConfirmationController;
import de.adorsys.aspsp.xs2a.web.ConsentInformationController;
import de.adorsys.aspsp.xs2a.web.PaymentInitiationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HeadersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersFactory.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<Class, Class> controllerClassMap = new HashMap<>();

    static {
        controllerClassMap.put(AccountController.class, AccountRequestHeader.class);
        controllerClassMap.put(ConsentInformationController.class, ConsentRequestHeader.class);
        controllerClassMap.put(PaymentInitiationController.class, PaymentInitiationRequestHeader.class);
        controllerClassMap.put(FundsConfirmationController.class, FundsConfirmationRequestHeader.class);
    }

    public static RequestHeaders getHeadersImpl(Map<String, String> requestHeadersMap, Class controllerClass) {
        Class<? extends RequestHeaders> headerClass = controllerClassMap.get(controllerClass);

        if (headerClass == null) {
            return new NotMatchedHeaderImpl();
        } else {
            try {
                return MAPPER.convertValue(requestHeadersMap, headerClass);
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Error request headers conversion: " + exception.getMessage());
                return new ErrorMessageHeaderImpl(exception.getMessage());
            }
        }
    }
}
