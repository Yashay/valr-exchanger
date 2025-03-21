package org.valr.util.enums;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValidEnum, Object> {
    private String[] acceptedValues;

    @Override
    public void initialize(ValidEnum annotation) {
        acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return false;

        if (value instanceof String stringValue) {
            return Arrays.asList(acceptedValues).contains(stringValue.toUpperCase());
        }

        if (value instanceof Enum<?>) {
            return Arrays.asList(acceptedValues).contains(((Enum<?>) value).name());
        }

        return false;
    }
}