package de.adorsys.aspsp.xs2a.spi.domain.headers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.spi.domain.headers.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HeadersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersFactory.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static RequestHeaders getHeadersImpl(Map<String, String> requestHeadersMap, String handlerName) {
        RequestHeaders headers = new NotMatchedHeaderImpl();

        switch (handlerName) {
            case "de.adorsys.aspsp.xs2a.web.AccountController":
                headers = convertMapToHeaders(requestHeadersMap, AccountRequestHeader.class);
                break;
            case "de.adorsys.aspsp.xs2a.web.ConsentInformationController":
                headers = convertMapToHeaders(requestHeadersMap, ConsentRequestHeader.class);
                break;
            case "de.adorsys.aspsp.xs2a.web.PaymentInitiationController":
                headers = convertMapToHeaders(requestHeadersMap, PaymentInitiationRequestHeader.class);
                break;
            case "de.adorsys.aspsp.xs2a.web.ConfirmationFundsController":
                headers = convertMapToHeaders(requestHeadersMap, FundsConfirmationRequestHeader.class);
                break;
        }

        return headers;
    }

    private static RequestHeaders convertMapToHeaders(Map<String, String> requestHeadersMap, Class<? extends RequestHeaders> toValueType) {
        try {
            return MAPPER.convertValue(requestHeadersMap, toValueType);
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Error request headers conversion: {} ", ex.getMessage());
        }
        return new NotMatchedHeaderImpl();
    }
}
