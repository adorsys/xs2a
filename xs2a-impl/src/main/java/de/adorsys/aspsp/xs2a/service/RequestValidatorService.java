package de.adorsys.aspsp.xs2a.service;


import de.adorsys.aspsp.xs2a.spi.domain.headers.RequestHeaders;
import de.adorsys.aspsp.xs2a.spi.domain.headers.HeadersFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Service
public class RequestValidatorService {

    public boolean isRequestValid(HttpServletRequest request, Object handler) {
        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);

        boolean areRequestHeadersValid = areRequestHeadersValid(requestHeadersMap, handler);

        return areRequestHeadersValid;
    }

    private boolean areRequestHeadersValid(Map<String, String> requestHeadersMap, Object handler) {
        String handlerName = ((HandlerMethod) handler).getBeanType().getTypeName();
        RequestHeaders headerImpl = HeadersFactory.getHeadersImpl(requestHeadersMap, handlerName);

        return headerImpl.isValid();
    }

    private Map<String, String> getRequestHeadersMap(HttpServletRequest request) {

        Map<String, String> map = new HashMap<String, String>();

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }
}
