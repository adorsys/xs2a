package de.adorsys.aspsp.xs2a.service.validator;


import de.adorsys.aspsp.xs2a.service.validator.header.HeadersFactory;
import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import de.adorsys.aspsp.xs2a.service.validator.header.impl.ErrorMessageHeaderImpl;
import de.adorsys.aspsp.xs2a.service.validator.parameter.ParametersFactory;
import de.adorsys.aspsp.xs2a.service.validator.parameter.RequestParameter;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.ErrorMessageParameterImpl;
import lombok.extern.log4j.Log4j;
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
@Log4j
public class RequestValidatorService {
    private ParametersFactory parametersFactory;
    private Validator validator;

    @Autowired
    public RequestValidatorService(Validator validator, ParametersFactory parametersFactory) {
        this.validator = validator;
        this.parametersFactory = parametersFactory;
    }

    public Map<String, String> getRequestViolationMap(HttpServletRequest request, Object handler) {
        Map<String, String> violationMap = new HashMap<>();
        violationMap.putAll(getRequestHeaderViolationMap(request, handler));
        violationMap.putAll(getRequestParametersViolationMap(request, handler));

        return violationMap;
    }

    public Map<String, String> getRequestParametersViolationMap(HttpServletRequest request, Object handler) {

        Map<String, String> requestParameterMap = getRequestParametersMap(request);

        RequestParameter parameterImpl = parametersFactory.getParameterImpl(requestParameterMap, ((HandlerMethod) handler).getBeanType());

        if (parameterImpl instanceof ErrorMessageParameterImpl) {
            return Collections.singletonMap("Wrong parameters : ", ((ErrorMessageParameterImpl) parameterImpl).getErrorMessage());
        }

        Map<String, String> requestParameterViolationsMap = validator.validate(parameterImpl).stream()
                                                            .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return requestParameterViolationsMap;
    }

    public Map<String, String> getRequestHeaderViolationMap(HttpServletRequest request, Object handler) {

        Map<String, String> requestHeadersMap = getRequestHeadersMap(request);

        RequestHeader headerImpl = HeadersFactory.getHeadersImpl(requestHeadersMap, ((HandlerMethod) handler).getBeanType());

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

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            requestHeaderMap.put(key, value);
        }

        return requestHeaderMap;
    }

    private Map<String, String> getRequestParametersMap(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
               .collect(Collectors.toMap(
               Map.Entry::getKey,
               e -> String.join(",", e.getValue())));
    }
}
