package de.adorsys.aspsp.xs2a.web.interceptor;

import de.adorsys.aspsp.xs2a.service.RequestValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

            final List<String> violations = requestHeaderViolationsMap.entrySet().stream()
                                   .map(entry -> entry.getKey() + " : " + entry.getValue()).collect(Collectors.toList());

            LOGGER.debug(violations.toString());

            response.sendError(FORMAT_ERROR.getCode(), violations.toString());
            return false;
        }
    }
}
