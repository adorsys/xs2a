package de.adorsys.aspsp.xs2a.service.validator.header;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.*;
import de.adorsys.aspsp.xs2a.web.*;
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
        controllerClassMap.put(PaymentInitiationController.class, PaymentRequestHeader.class);
        controllerClassMap.put(BulkPaymentInitiationController.class, PaymentInitiationRequestHeader.class);
        controllerClassMap.put(PeriodicPaymentsController.class, PaymentInitiationRequestHeader.class);
        controllerClassMap.put(FundsConfirmationController.class, FundsConfirmationRequestHeader.class);
    }

    public static RequestHeader getHeadersImpl(Map<String, String> requestHeadersMap, Class controllerClass) {
        Class<? extends RequestHeader> headerClass = controllerClassMap.get(controllerClass);

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
