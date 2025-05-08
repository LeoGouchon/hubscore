package com.leogouchon.squashapp.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.leogouchon.squashapp.model.types.MatchPoint;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter()
public class PointListConverter implements AttributeConverter<List<MatchPoint>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<MatchPoint> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur de conversion vers JSON", e);
        }
    }

    @Override
    public List<MatchPoint> convertToEntityAttribute(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Erreur de lecture JSON", e);
        }
    }
}