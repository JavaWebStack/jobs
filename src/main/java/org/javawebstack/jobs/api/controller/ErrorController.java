package org.javawebstack.jobs.api.controller;

import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.abstractdata.AbstractPrimitive;
import org.javawebstack.httpserver.Exchange;
import org.javawebstack.httpserver.handler.ExceptionHandler;
import org.javawebstack.jobs.api.response.Response;
import org.javawebstack.validator.ValidationError;
import org.javawebstack.validator.ValidationException;
import org.javawebstack.validator.ValidationResult;

public class ErrorController implements ExceptionHandler {

    public Object handle(Exchange exchange, Throwable throwable) {
        if(throwable instanceof ValidationException) {
            ValidationResult result = ((ValidationException) throwable).getResult();
            AbstractObject resultObj = new AbstractObject();
            result.getErrorMap().forEach((k, el) -> resultObj.set(k, el.stream().map(ValidationError::getMessage).collect(AbstractArray.collect(AbstractPrimitive::new))));
            return Response.error(400, "Validation Error").setData(resultObj);
        }
        throwable.printStackTrace();
        return Response.error(500, "Server Error");
    }

}
