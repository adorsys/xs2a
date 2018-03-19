package de.adorsys.aspsp.xs2a.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidHeadersValidator implements ConstraintValidator<ValidHeaders, String> {
    
    
    @Override
    public void initialize(ValidHeaders validHeaders) {
        
        System.out.println(" initialize ====");
        
    }
    
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}
