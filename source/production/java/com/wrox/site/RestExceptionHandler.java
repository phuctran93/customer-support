package com.wrox.site;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler
{
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handle(ConstraintViolationException e)
    {
        ErrorResponse errors = new ErrorResponse();
        for(ConstraintViolation violation : e.getConstraintViolations())
        {
            ErrorItem error = new ErrorItem();
            error.setCode(violation.getMessageTemplate());
            error.setMessage(violation.getMessage());
            errors.addError(error);
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @SuppressWarnings("unused")
    public static class ErrorItem
    {
        private String code;
        private String message;

        public String getCode()
        {
            return code;
        }

        public void setCode(String code)
        {
            this.code = code;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }
    }

    @SuppressWarnings("unused")
    public static class ErrorResponse
    {
        private List<ErrorItem> errors = new ArrayList<>();

        public List<ErrorItem> getErrors()
        {
            return errors;
        }

        public void setErrors(List<ErrorItem> errors)
        {
            this.errors = errors;
        }

        public void addError(ErrorItem error)
        {
            this.errors.add(error);
        }
    }
}
