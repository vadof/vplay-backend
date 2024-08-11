package com.vcasino.clicker.entity.converter;

import com.vcasino.clicker.entity.ConditionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConditionTypeConverter implements AttributeConverter<ConditionType, String> {

    @Override
    public String convertToDatabaseColumn(ConditionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getType();
    }

    @Override
    public ConditionType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ConditionType.fromString(dbData);
    }
}
