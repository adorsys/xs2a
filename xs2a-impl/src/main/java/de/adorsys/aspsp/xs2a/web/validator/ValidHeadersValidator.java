package de.adorsys.aspsp.xs2a.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;

public class ValidHeadersValidator implements ConstraintValidator<ValidHeaders, String> {
    
    
    @Override
    public void initialize(ValidHeaders s) {
    
    }
    
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
