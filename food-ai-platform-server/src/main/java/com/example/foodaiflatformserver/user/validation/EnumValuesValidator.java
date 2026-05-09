package com.example.foodaiflatformserver.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValuesValidator implements ConstraintValidator<EnumValues, Collection<String>> {

    private Set<String> allowedValues;

    @Override
    public void initialize(EnumValues annotation) {
        allowedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }

        return values.stream().allMatch(allowedValues::contains);
    }
}
