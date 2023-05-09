package com.onlineshop.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class JacksonAttributeConverter<T> implements AttributeConverter<T> {

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
  }

  private final Class<T> clazz;

  public JacksonAttributeConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public AttributeValue transformFrom(T input) {
    try {
      return AttributeValue
          .builder()
          .s(mapper.writeValueAsString(input))
          .build();
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Unable to serialize object", e);
    }
  }

  @Override
  public T transformTo(AttributeValue input) {
    try {
      return mapper.readValue(input.s(), this.clazz);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException("Unable to parse object", e);
    }
  }

  @Override
  public EnhancedType type() {
    return EnhancedType.of(this.clazz);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}
