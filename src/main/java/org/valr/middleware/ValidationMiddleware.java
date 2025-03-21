package org.valr.middleware;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;

public class ValidationMiddleware<T> {
    private Validator validator;
    private Class<T> targetClass;

    public ValidationMiddleware(Class<T> targetClass) {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
        this.targetClass = targetClass;
    }

    public void validate(RoutingContext context) {
        JsonObject data = context.body().asJsonObject();
        if (data == null) {
            context.response()
                    .setStatusCode(400)
                    .end("Invalid request format: body is missing or not a valid JSON.");
            return;
        }

        try {
            T object = data.mapTo(targetClass);
            Set<ConstraintViolation<T>> violations = validator.validate(object);

            if (!violations.isEmpty()) {
                StringBuilder errorMsg = new StringBuilder();
                for (ConstraintViolation<T> violation : violations) {
                    errorMsg.append(violation.getMessage()).append("\n");
                }
                context.response()
                        .setStatusCode(400)
                        .end("Validation errors:\n" + errorMsg.toString());
            } else {
                context.next();
            }
        } catch (Exception e) {
            context.response()
                    .setStatusCode(400)
                    .end("Invalid request format: " + e.getMessage());
        }
    }
}