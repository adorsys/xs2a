package de.adorsys.aspsp.xs2a.service.validator.parameter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.AccountRequestParameter;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.ErrorMessageParameterImpl;
import de.adorsys.aspsp.xs2a.service.validator.parameter.impl.NotMatchedParameterImpl;
import de.adorsys.aspsp.xs2a.web.AccountController;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class ParametersFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParametersFactory.class);
    private final ObjectMapper objectMapper;

    private static final Map<Class, Class> controllerClassMap = new HashMap<>();

    static {
        controllerClassMap.put(AccountController.class, AccountRequestParameter.class);
    }

    public RequestParameter getParameterImpl(Map<String, String> requestParametersMap, Class controllerClass) {
        Class<? extends RequestParameter> headerClass = controllerClassMap.get(controllerClass);

        if (headerClass == null) {
            return new NotMatchedParameterImpl();
        } else {
            try {
                return objectMapper.convertValue(requestParametersMap, headerClass);
            } catch (IllegalArgumentException exception) {
                LOGGER.error("Error request parameter conversion: " + exception.getMessage());
                return new ErrorMessageParameterImpl(exception.getMessage());
            }
        }
    }
}
