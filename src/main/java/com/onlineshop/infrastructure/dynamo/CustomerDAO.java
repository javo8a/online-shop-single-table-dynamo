package com.onlineshop.infrastructure.dynamo;

import com.onlineshop.infrastructure.util.JacksonAttributeConverter;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.OffsetDateTimeAsStringAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.UuidAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Data
public class CustomerDAO {
  public static final String ENTITY_TYPE = "Customer";
  static final BiConsumer<CustomerDAO, String> NO_OP = (__, ___) -> {};
  public static final String CUSTOMER_PREFIX = "c#";
  static final Function<CustomerDAO, String> PK_OP = (c) -> CUSTOMER_PREFIX + c.getId();
  static final Function<CustomerDAO, String> SK_OP = (c) -> CUSTOMER_PREFIX;
  static final Function<CustomerDAO, String> GS1_PK_OP = (c) -> "customer";
  static final Function<CustomerDAO, String> GS1_SK_OP = (c) -> CUSTOMER_PREFIX + c.getId();
  
  static final Function<CustomerDAO, String> GET_TYPE_OP = (c) -> ENTITY_TYPE;
  static final BiConsumer<CustomerDAO, String> SET_TYPE_OP =
      (c, type) -> {
        if (!ENTITY_TYPE.equals(type)) {
          throw new IllegalArgumentException(
              "Attempted to marshall into CustomerDAO a row of Type=" + type);
        }
      };

  public static String prefixedId(UUID id) {
    return CUSTOMER_PREFIX + id;
  }

  private UUID id;
  private String name;
  private AddressDAO address;
  private OffsetDateTime lastOrder;

  public static TableSchema<CustomerDAO> schema() {
    return StaticTableSchema.builder(CustomerDAO.class)
        .newItemSupplier(CustomerDAO::new)
        .addAttribute(
            String.class,
            a ->
                a.name("PK")
                    .getter(PK_OP)
                    .setter(NO_OP)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
        .addAttribute(
            String.class,
            a ->
                a.name("SK").getter(SK_OP).setter(NO_OP).tags(StaticAttributeTags.primarySortKey()))
        .addAttribute(String.class, a -> a.name("type").getter(GET_TYPE_OP).setter(SET_TYPE_OP))
        .addAttribute(
            UUID.class,
            a ->
                a.name("id")
                    .attributeConverter(UuidAttributeConverter.create())
                    .getter(CustomerDAO::getId)
                    .setter(CustomerDAO::setId))
        .addAttribute(
            String.class,
            a -> a.name("name").getter(CustomerDAO::getName).setter(CustomerDAO::setName))
        .addAttribute(
            AddressDAO.class,
            a ->
                a.name("address")
                    .attributeConverter(new JacksonAttributeConverter<>(AddressDAO.class))
                    .getter(CustomerDAO::getAddress)
                    .setter(CustomerDAO::setAddress))
        .addAttribute(
            OffsetDateTime.class,
            a ->
                a.name("last_order")
                    .attributeConverter(OffsetDateTimeAsStringAttributeConverter.create())
                    .getter(CustomerDAO::getLastOrder)
                    .setter(CustomerDAO::setLastOrder))
        .addAttribute(
            String.class,
            a ->
                a.name("GS1PK")
                    .getter(GS1_PK_OP)
                    .setter(NO_OP)
                    .tags(StaticAttributeTags.secondaryPartitionKey("GS1")))
        .addAttribute(
            String.class,
            a ->
                a.name("GS1SK")
                    .getter(GS1_SK_OP)
                    .setter(NO_OP)
                    .tags(StaticAttributeTags.secondarySortKey("GS1")))
        .build();
  }

  public record AddressDAO(String street, String city, String state, String zipcode) {}
}
