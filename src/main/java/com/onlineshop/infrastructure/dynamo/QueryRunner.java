package com.onlineshop.infrastructure.dynamo;

import static java.util.Objects.isNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.api.dto.*;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

@ApplicationScoped
public class QueryRunner {

  public final DynamoDbTable<OrderDAO> orderTable;
  public final DynamoDbTable<ProductDAO> productTable;
  public final DynamoDbTable<CustomerDAO> customerTable;
  public final DynamoDbTable<OrderItemDAO> orderItemTable;
  public final ObjectMapper objectMapper;

  public final DynamoDBHandler dynamoDBHandler;

  public QueryRunner(DynamoDBHandler dynamoDBHandler, ObjectMapper objectMapper) {

    this.dynamoDBHandler = dynamoDBHandler;
    orderTable = dynamoDBHandler.getOrderTable();
    productTable = dynamoDBHandler.getProductTable();
    customerTable = dynamoDBHandler.getCustomerTable();
    orderItemTable = dynamoDBHandler.getOrderItemTable();
    this.objectMapper = objectMapper;
  }

  public Page<OrderDAO> getCustomerOrderDAOPage(
      UUID customerId, Map<String, AttributeValue> exclusiveStartKey, Integer pageSize) {
    DynamoDbIndex<OrderDAO> tableIndex = orderTable.index("GS2");
    QueryConditional queryConditional =
        QueryConditional.sortGreaterThan(
            s -> s.partitionValue("c#" + customerId).sortValue("2020-10-10"));
    return queryForPage(tableIndex, queryConditional, exclusiveStartKey, pageSize);
  }

  public Page<OrderDAO> getOrderDAOPage(
      Map<String, AttributeValue> exclusiveStartKey, Integer pageSize) {
    DynamoDbIndex<OrderDAO> tableIndex = orderTable.index("GS1");
    QueryConditional queryConditional =
        QueryConditional.sortGreaterThan(s -> s.partitionValue("o#").sortValue("2020-10-10"));
    return queryForPage(tableIndex, queryConditional, exclusiveStartKey, pageSize);
  }

  public CustomerDAO getCustomerDAO(UUID customerId) {
    CustomerDAO customerDAO =
        customerTable.getItem(
            Key.builder()
                .partitionValue(CustomerDAO.prefixedId(customerId))
                .sortValue(CustomerDAO.CUSTOMER_PREFIX)
                .build());
    return customerDAO;
  }

  public Page<CustomerDAO> getCustomerDAOPage(
      Map<String, AttributeValue> exclusiveStartKey, Integer pageSize) {
    DynamoDbIndex<CustomerDAO> tableIndex = customerTable.index("GS1");
    QueryConditional queryConditional =
        QueryConditional.sortGreaterThan(
            s -> s.partitionValue("customer").sortValue(CustomerDAO.CUSTOMER_PREFIX));
    Page<CustomerDAO> firstPage =
        queryForPage(tableIndex, queryConditional, exclusiveStartKey, pageSize);
    return firstPage;
  }

  public ProductDAO getProductDAO(UUID productId) {
    return productTable.getItem(
        Key.builder()
            .partitionValue(ProductDAO.prefixedId(productId))
            .sortValue(ProductDAO.PRODUCT_PREFIX)
            .build());
  }

  public Page<ProductDAO> getProductDAOPage(
      Map<String, AttributeValue> exclusiveStartKey, Integer pageSize) {
    DynamoDbIndex<ProductDAO> tableIndex = productTable.index("GS1");
    QueryConditional queryConditional =
        QueryConditional.sortGreaterThan(
            s -> s.partitionValue("product").sortValue(ProductDAO.PRODUCT_PREFIX));
    Page<ProductDAO> firstPage =
        queryForPage(tableIndex, queryConditional, exclusiveStartKey, pageSize);
    return firstPage;
  }

  public OrderDAO getOrderDetailedDAO(UUID orderId) {
    AttributeValue orderPK = AttributeValue.builder().s(OrderDAO.ORDER_PREFIX + orderId).build();
    var queryRequest =
        QueryRequest.builder()
            .tableName(dynamoDBHandler.getTableName())
            .keyConditionExpression("#pk = :pk")
            .expressionAttributeNames(Map.of("#pk", "PK"))
            .expressionAttributeValues(Map.of(":pk", orderPK))
            .scanIndexForward(false)
            .limit(1 + 20)
            .build();
    QueryResponse queryResponse = dynamoDBHandler.query(queryRequest);

    // The result is a list of items in DynamoDB JSON format
    List<Map<String, AttributeValue>> items = queryResponse.items();

    OrderDAO orderDAO = null;
    List<OrderItemDAO> orderItemDAOS = new ArrayList<>();
    CustomerOrderDAO customerOrderDAO = null;
    for (Map<String, AttributeValue> item : items) {

      // Every item must have a 'Type' Attribute
      AttributeValue type = item.get("type");

      if (isNull(type) || isNull(type.s()) || type.s().isEmpty()) {
        throw new RuntimeException(
            "Required attribute 'Type' is missing or empty on Item with attributes: " + item);
      }

      // Switch on the 'Type' and use the respective TableSchema
      // to marshall the DynamoDB JSON into the value class
      switch (type.s()) {
        case OrderDAO.ENTITY_TYPE:
          orderDAO = OrderDAO.schema().mapToItem(item);
          break;
        case OrderItemDAO.ENTITY_TYPE:
          orderItemDAOS.add(OrderItemDAO.schema().mapToItem(item));
          break;
        case CustomerOrderDAO.ENTITY_TYPE:
          customerOrderDAO = CustomerOrderDAO.schema().mapToItem(item);
          break;
        default:
          throw new RuntimeException(
              String.format("Found unhandled Type=%s on Item with attributes: %s", type.s(), item));
      }
    }

    if (orderDAO != null) {
      orderDAO.setItems(orderItemDAOS);
      orderDAO.setCustomer(customerOrderDAO);
    }
    return orderDAO;
  }

  public <T> Page<T> queryForPage(
      DynamoDbIndex<T> tableIndex,
      QueryConditional queryConditional,
      Map<String, AttributeValue> exclusiveStartKey,
      Integer pageSize) {
    PageIterable<T> pages =
        PageIterable.create(
            tableIndex.query(
                QueryEnhancedRequest.builder()
                    .limit(pageSize)
                    .exclusiveStartKey(exclusiveStartKey)
                    .queryConditional(queryConditional)
                    .build()));
    return pages.stream().limit(1).findFirst().get();
  }
}
