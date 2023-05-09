package com.onlineshop.infrastructure.dynamo;

import com.onlineshop.infrastructure.util.JacksonAttributeConverter;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.UuidAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Data
public class CustomerOrderDAO {

  public static final String ENTITY_TYPE = "CustomerOrder";
  static final BiConsumer<CustomerOrderDAO, String> NO_OP = (__, ___) -> {};
  public static final String CUSTOMER_ORDER_PREFIX = "c#";
  static final Function<CustomerOrderDAO, String> PK_OP =
      (c) -> OrderDAO.ORDER_PREFIX + c.getOrderId();
  static final Function<CustomerOrderDAO, String> SK_OP = (c) -> CUSTOMER_ORDER_PREFIX;

  static final Function<CustomerOrderDAO, String> GET_TYPE_OP = (c) -> ENTITY_TYPE;
  static final BiConsumer<CustomerOrderDAO, String> SET_TYPE_OP =
      (c, type) -> {
        if (!ENTITY_TYPE.equals(type)) {
          throw new IllegalArgumentException(
              "Attempted to marshall into CustomerOrderDAO a row of Type=" + type);
        }
      };

  private UUID orderId;
  private UUID customerId;
  private String name;
  private AddressDAO address;

  public static TableSchema<CustomerOrderDAO> schema() {
    return StaticTableSchema.builder(CustomerOrderDAO.class)
        .newItemSupplier(CustomerOrderDAO::new)
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
                a.name("orderId")
                    .attributeConverter(UuidAttributeConverter.create())
                    .getter(CustomerOrderDAO::getOrderId)
                    .setter(CustomerOrderDAO::setOrderId))
        .addAttribute(
            String.class,
            a -> a.name("name").getter(CustomerOrderDAO::getName).setter(CustomerOrderDAO::setName))
        .addAttribute(
            CustomerOrderDAO.AddressDAO.class,
            a ->
                a.name("address")
                    .attributeConverter(
                        new JacksonAttributeConverter<>(CustomerOrderDAO.AddressDAO.class))
                    .getter(CustomerOrderDAO::getAddress)
                    .setter(CustomerOrderDAO::setAddress))
        .build();
  }

  public record AddressDAO(String street, String city, String state, String zipcode) {}
}
