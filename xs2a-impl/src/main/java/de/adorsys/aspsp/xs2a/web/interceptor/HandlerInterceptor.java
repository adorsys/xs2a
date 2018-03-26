package de.adorsys.aspsp.xs2a.web.interceptor;

import de.adorsys.aspsp.xs2a.service.RequestValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static de.adorsys.aspsp.xs2a.spi.domain.MessageCode.FORMAT_ERROR;

@Component
public class HandlerInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerInterceptor.class);

    private RequestValidatorService requestValidatorService;

    public HandlerInterceptor(RequestValidatorService requestValidatorService) {
        this.requestValidatorService = requestValidatorService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return isRequestValidAndSendRespIfError(request, response, handler);
    }

    private boolean isRequestValidAndSendRespIfError(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Map<String, String> requestHeaderViolationsMap = requestValidatorService.getRequestHeaderViolationMap(request, handler);

        if (requestHeaderViolationsMap.isEmpty()) {
            return true;
        } else {
            StringBuilder builderViolationsText = new StringBuilder("Request header has violation: ");

            requestHeaderViolationsMap.forEach((key, value) -> builderViolationsText.append(key).append(" ").append(value).append("; "));

            LOGGER.debug(builderViolationsText.toString());

            response.sendError(FORMAT_ERROR.getCode(), builderViolationsText.toString());
            return false;
        }
    }
}
