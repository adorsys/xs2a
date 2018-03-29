package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.entityValidator.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;
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

    public void validate(EntityValidator objectForValidate) {
        final List<String> violations = validator.validate(objectForValidate).stream()
                                        .map(vl -> vl.getPropertyPath().toString() + " : " + vl.getMessage())
                                        .collect(Collectors.toList());

        if (violations.size() > 0) {
            LOGGER.debug(violations.toString());
            throw new ValidationException(FORMAT_ERROR.name() + ": " + violations);
        }
    }
}
