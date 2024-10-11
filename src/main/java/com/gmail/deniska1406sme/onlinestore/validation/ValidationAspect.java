package com.gmail.deniska1406sme.onlinestore.validation;


import com.gmail.deniska1406sme.onlinestore.exceptions.ValidationException;
import com.gmail.deniska1406sme.onlinestore.utils.ValidationUtil;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

@Aspect
@Component
public class ValidationAspect {
    @Pointcut("execution (* com.gmail.deniska1406sme.onlinestore.controllers..*(..)) && args(..,bindingResult)")
    public void methodsWithBindingResult(BindingResult bindingResult) {
    }

    @Before("methodsWithBindingResult(bindingResult)")
    public void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException(ValidationUtil.handleValidationErrors(bindingResult));
        }
    }
}
