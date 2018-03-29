package de.adorsys.aspsp.xs2a.service;


import de.adorsys.aspsp.xs2a.domain.entityValidator.TransactionByPeriodValidator;
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
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.FORMAT_ERROR;

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

        Map<String, String> requestHeaderViolationsMap = validator.validate(headerImpl).stream().collect(
        Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        return requestHeaderViolationsMap;
    }

    private Map<String, String> getRequestHeadersMap(HttpServletRequest request) {

        Map<String, String> requestHeaderMap = new HashMap<String, String>();
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

    public void validateTransactionByPeriodParameters(String accountId, Date dateFrom, Date dateTo) {
        Map<String, String> requestViolationsMap = validator.validate(new TransactionByPeriodValidator(accountId, dateFrom, dateTo)).stream().collect(
        Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        if (requestViolationsMap.size() > 0) {
            final List<String> violations = requestViolationsMap.entrySet().stream()
                                            .map(entry -> entry.getKey() + " : " + entry.getValue()).collect(Collectors.toList());

            LOGGER.debug(violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations.toString());
        }
    }
}
