package de.adorsys.aspsp.xs2a.web.validator;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

public class HeadersValidator extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Enumeration<String> headerNames = request.getHeaderNames();
        isHeadersValid(request);
        return true;
    }
    
    public boolean isHeadersValid(HttpServletRequest request) {
        System.out.println("=== isValid === ");
        return true;
    }
}
