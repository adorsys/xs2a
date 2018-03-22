package de.adorsys.aspsp.xs2a.web.interceptor;

import de.adorsys.aspsp.xs2a.service.RequestValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static de.adorsys.aspsp.xs2a.spi.domain.MessageCode.FORMAT_ERROR;

public class HandlerInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerInterceptor.class);

    private RequestValidatorService requestValidatorService;

    public HandlerInterceptor(RequestValidatorService requestValidatorService) {
        this.requestValidatorService = requestValidatorService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (requestValidatorService.isRequestValid(request, handler)) {
            return true;
        } else {
            LOGGER.error("Bad request. Request is not valid!");
            response.sendError(FORMAT_ERROR.getCode());
            return false;
        }
    }
}
