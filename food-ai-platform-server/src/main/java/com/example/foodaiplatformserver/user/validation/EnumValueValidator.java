package com.example.foodaiplatformserver.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Set<String> allowedValues;

    @Override
    public void initialize(EnumValue annotation) {
        allowedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return allowedValues.contains(value);
    }
}
