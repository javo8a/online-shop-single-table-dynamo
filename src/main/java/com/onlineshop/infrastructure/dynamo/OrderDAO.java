package com.onlineshop.infrastructure.dynamo;

import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primarySortKey;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.OffsetDateTimeAsStringAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.UuidAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Data
public class OrderDAO {

  public static final String ENTITY_TYPE = "Order";
  static final BiConsumer<OrderDAO, String> NO_OP = (__, ___) -> {};

  public static final String ORDER_PREFIX = "o#";
  static final Function<OrderDAO, String> PK_OP = (o) -> ORDER_PREFIX + o.id;
  static final Function<OrderDAO, String> SK_OP = (o) -> ORDER_PREFIX;
  static final Function<OrderDAO, String> GS1_PK_OP = (o) -> ORDER_PREFIX;
  static final Function<OrderDAO, String> GS1_SK_OP =
      (o) -> o.created.format(DateTimeFormatter.ISO_DATE);
  static final Function<OrderDAO, String> GS2_PK_OP =
      (o) -> CustomerDAO.CUSTOMER_PREFIX + o.customerId;
  static final Function<OrderDAO, String> GS2_SK_OP = (o) -> ORDER_PREFIX + o.id;
  static final Function<OrderDAO, String> GET_TYPE_OP = (o) -> ENTITY_TYPE;
  static final BiConsumer<OrderDAO, String> SET_TYPE_OP =
      (o, type) -> {
        if (!ENTITY_TYPE.equals(type)) {
          throw new IllegalArgumentException(
              "Attempted to marshall into OrderDAO a row of Type=" + type);
        }
      };

  public static String prefixedId(UUID id) {
    return ORDER_PREFIX + id;
  }

  private UUID id;
  private UUID customerId;
  private BigDecimal total;
  private OffsetDateTime created;
  private OffsetDateTime lastUpdated;
  private CustomerOrderDAO customer;
  private List<OrderItemDAO> items = new ArrayList<>();

  public static StaticTableSchema<OrderDAO> schema() {

    return StaticTableSchema.builder(OrderDAO.class)
        .newItemSupplier(OrderDAO::new)
        .addAttribute(
            String.class, a -> a.name("PK").getter(PK_OP).setter(NO_OP).tags(primaryPartitionKey()))
        .addAttribute(
            String.class, a -> a.name("SK").getter(SK_OP).setter(NO_OP).tags(primarySortKey()))
        .addAttribute(String.class, a -> a.name("type").getter(GET_TYPE_OP).setter(SET_TYPE_OP))
        .addAttribute(
            UUID.class,
            a ->
                a.name("id")
                    .attributeConverter(UuidAttributeConverter.create())
                    .getter(OrderDAO::getId)
                    .setter(OrderDAO::setId))
        .addAttribute(
            UUID.class,
            a ->
                a.name("customer_id")
                    .getter(OrderDAO::getCustomerId)
                    .setter(OrderDAO::setCustomerId))
        .addAttribute(
            BigDecimal.class,
            a -> a.name("total").getter(OrderDAO::getTotal).setter(OrderDAO::setTotal))
        .addAttribute(
            OffsetDateTime.class,
            a ->
                a.name("created")
                    .attributeConverter(OffsetDateTimeAsStringAttributeConverter.create())
                    .getter(OrderDAO::getCreated)
                    .setter(OrderDAO::setCreated))
        .addAttribute(
            OffsetDateTime.class,
            a ->
                a.name("last_updated")
                    .attributeConverter(OffsetDateTimeAsStringAttributeConverter.create())
                    .getter(OrderDAO::getLastUpdated)
                    .setter(OrderDAO::setLastUpdated))
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
        .addAttribute(
            String.class,
            a ->
                a.name("GS2PK")
                    .getter(GS2_PK_OP)
                    .setter(NO_OP)
                    .tags(StaticAttributeTags.secondaryPartitionKey("GS2")))
        .addAttribute(
            String.class,
            a ->
                a.name("GS2SK")
                    .getter(GS2_SK_OP)
                    .setter(NO_OP)
                    .tags(StaticAttributeTags.secondarySortKey("GS2")))
        .build();
  }
}
