package de.adorsys.aspsp.xs2a.service.validator;


import de.adorsys.aspsp.xs2a.domain.headers.HeadersFactory;
import de.adorsys.aspsp.xs2a.domain.headers.RequestHeaders;
import de.adorsys.aspsp.xs2a.domain.headers.impl.ErrorMessageHeaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequestValidatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidatorService.class);

    private Validator validator;

    @Autowired
    public RequestValidatorService(Validator validator) {
        this.validator = validator;
    }

    public Map<String, String> getRequestHeaderViolationMap(HttpServletRequest request, Object handler) {

        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);

        RequestHeaders headerImpl = HeadersFactory.getHeadersImpl(requestHeadersMap, ((HandlerMethod) handler).getBeanType());

        if (headerImpl instanceof ErrorMessageHeaderImpl) {
            return Collections.singletonMap("Wrong header arguments: ", ((ErrorMessageHeaderImpl) headerImpl).getErrorMessage());
        }

        Map<String, String> requestHeaderViolationsMap = validator.validate(headerImpl).stream()
                                                         .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return requestHeaderViolationsMap;
    }

    private Map<String, String> getRequestHeadersMap(HttpServletRequest request) {

        Map<String, String> requestHeaderMap = new HashMap<>();
        if (request == null) {
            return requestHeaderMap;
        }

        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            requestHeaderMap.put(key, value);
        }

        return requestHeaderMap;
    }
}
