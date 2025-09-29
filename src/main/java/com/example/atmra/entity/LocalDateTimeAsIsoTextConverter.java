package com.example.atmra.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Конвертер даты для поля даты в БД SQLite.
 */
@Converter(autoApply = true)
public class LocalDateTimeAsIsoTextConverter implements AttributeConverter<LocalDateTime, String> {

    /** Формат двты */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : FMT.format(attribute);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalDateTime.parse(dbData);
    }
}
