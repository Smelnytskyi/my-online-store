package com.gmail.deniska1406sme.onlinestore.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.gmail.deniska1406sme.onlinestore..*(..)) && " +
            "!execution(* com.gmail.deniska1406sme.onlinestore.config..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Object[] filteredArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            filteredArgs[i] = checkSensitiveData(args[i]);
        }
        logger.info("Method call: {} with arguments: {}", joinPoint.getSignature().getName(), Arrays.toString(filteredArgs));
    }

    @AfterReturning(pointcut = "execution(* com.gmail.deniska1406sme.onlinestore..*(..)) && " +
            "!execution(* com.gmail.deniska1406sme.onlinestore.config..*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        Object filteredResult = checkSensitiveData(result);
        logger.info("Method {} completed successfully, with result: {}", joinPoint.getSignature().getName(), filteredResult);
    }

    @AfterThrowing(pointcut = "execution(* com.gmail.deniska1406sme.onlinestore..*(..)) && " +
            "!execution(* com.gmail.deniska1406sme.onlinestore.config..*(..))", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        logger.error("Method {} threw an exception: {}", joinPoint.getSignature().getName(), exception.getMessage());//, exception); for extended stack trace
    }

    private Object checkSensitiveData(Object arg) {
        if (arg instanceof String) {
            String strArg = (String) arg;
            if (strArg.toLowerCase().contains("password")) {
                return "****";
            }
            if (strArg.split("\\.").length == 3) {
                return "*****";
            }
        }
        if (arg instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) arg;
            Object body = responseEntity.getBody();
            if (body instanceof Map) {
                Map<?, ?> bodyMap = (Map<?, ?>) body;
                Map<Object, Object> filteredBody = new HashMap<Object, Object>();
                for (Map.Entry<?, ?> entry : bodyMap.entrySet()) {
                    if (entry.getKey().toString().toLowerCase().contains("token")) {
                        filteredBody.put(entry.getKey(), "********");
                    } else {
                        filteredBody.put(entry.getKey(), entry.getValue());
                    }
                }
                return new ResponseEntity<>(filteredBody, responseEntity.getStatusCode());
            }
        }
        return arg;
    }
}
