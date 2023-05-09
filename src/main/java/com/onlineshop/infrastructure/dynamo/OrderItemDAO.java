package com.onlineshop.infrastructure.dynamo;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.UuidAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Data
public class OrderItemDAO {

  public static final String ENTITY_TYPE = "OrderItem";
  static final BiConsumer<OrderItemDAO, String> NO_OP = (__, ___) -> {};

  public static final String ORDER_ITEM_PREFIX = "oi#";
  static final Function<OrderItemDAO, String> PK_OP =
      (item) -> OrderDAO.ORDER_PREFIX + item.orderId;
  static final Function<OrderItemDAO, String> SK_OP = (item) -> ORDER_ITEM_PREFIX + item.productId;
  static final Function<OrderItemDAO, String> GS1_PK_OP = (item) -> ORDER_ITEM_PREFIX;
  static final Function<OrderItemDAO, String> GS1_SK_OP =
      (item) -> ProductDAO.PRODUCT_PREFIX + item.productId;
  static final Function<OrderItemDAO, String> GET_TYPE_OP = (item) -> ENTITY_TYPE;
  static final BiConsumer<OrderItemDAO, String> SET_TYPE_OP =
      (item, type) -> {
        if (!ENTITY_TYPE.equals(type)) {
          throw new IllegalArgumentException(
              "Attempted to marshall into OrderItemDAO a row of Type=" + type);
        }
      };

  private UUID orderId;
  private UUID productId;
  private String name;
  private Integer quantity;
  private BigDecimal price;

  public static TableSchema<OrderItemDAO> schema() {
    return StaticTableSchema.builder(OrderItemDAO.class)
        .newItemSupplier(OrderItemDAO::new)
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
                    .getter(OrderItemDAO::getProductId)
                    .setter(OrderItemDAO::setProductId))
        .addAttribute(
            String.class,
            a -> a.name("name").getter(OrderItemDAO::getName).setter(OrderItemDAO::setName))
        .addAttribute(
            BigDecimal.class,
            a -> a.name("price").getter(OrderItemDAO::getPrice).setter(OrderItemDAO::setPrice))
        .addAttribute(
            Integer.class,
            a ->
                a.name("quantity")
                    .getter(OrderItemDAO::getQuantity)
                    .setter(OrderItemDAO::setQuantity))
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
}
