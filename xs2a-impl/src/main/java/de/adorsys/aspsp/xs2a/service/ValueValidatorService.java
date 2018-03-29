package de.adorsys.aspsp.xs2a.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.domain.MessageCode.FORMAT_ERROR;

@Service
public class ValueValidatorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueValidatorService.class);

    private Validator validator;

    @Autowired
    public ValueValidatorService(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object objectForValidate) {
        Map<String, String> violationsMap = validator.validate(objectForValidate).stream().collect(
        Collectors.toMap(violation -> violation.getPropertyPath().toString(), ConstraintViolation::getMessage));

        if (violationsMap.size() > 0) {
            final List<String> violations = violationsMap.entrySet().stream()
                                            .map(entry -> entry.getKey() + " : " + entry.getValue()).collect(Collectors.toList());

            LOGGER.debug(violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations.toString());
        }
    }
}
