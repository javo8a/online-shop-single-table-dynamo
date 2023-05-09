package com.onlineshop.infrastructure.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.onlineshop.infrastructure.util.JsonResourceObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Priority(1)
@ApplicationScoped
public class DynamoDBHandler {

  Logger logger = LoggerFactory.getLogger(DynamoDBHandler.class);

  @Getter
  @ConfigProperty(name = "online-shop.table.name", defaultValue = "ONLINE_SHOP")
  String tableName;

  private final DynamoDbClient dynamoDbClient;

  private final ObjectMapper objectMapper;

  // Amazon DynamoDB SDK with annotations cannot be used in Quarkus
  // (https://github.com/oracle/graal/issues/3386)
  // private TableSchema<OrderDAO> melRevisionTableSchema =
  // TableSchema.fromClass(MelRevision.class);

  @Getter private DynamoDbTable<OrderDAO> orderTable;
  @Getter private DynamoDbTable<ProductDAO> productTable;
  @Getter private DynamoDbTable<CustomerDAO> customerTable;
  @Getter private DynamoDbTable<OrderItemDAO> orderItemTable;
  @Getter private DynamoDbTable<CustomerOrderDAO> customerOrderTable;
  
  @Getter private DynamoDbEnhancedClient enhancedClient;


  public DynamoDBHandler(DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) {
    this.dynamoDbClient = dynamoDbClient;
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  void initDynamoDbEnhancedClient() {
    logger.info("> initDynamoDbEnhancedClient");
    enhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(dynamoDbClient).build();
    orderTable = enhancedClient.table(tableName, OrderDAO.schema());
    productTable = enhancedClient.table(tableName, ProductDAO.schema());
    customerTable = enhancedClient.table(tableName, CustomerDAO.schema());
    orderItemTable = enhancedClient.table(tableName, OrderItemDAO.schema());
    customerOrderTable = enhancedClient.table(tableName, CustomerOrderDAO.schema());
    this.createTableIfNotExists(enhancedClient);
  }

  public void createTableIfNotExists(DynamoDbEnhancedClient enhancedClient) {
    if (tableExists(this.tableName)) {
      logger.info("Table={} already exists", this.tableName);
    } else {
      // Global indexes GS1&GS2
      var gs1 =
          EnhancedGlobalSecondaryIndex.builder()
              .indexName("GS1")
              .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
              .build();
      var gs2 =
          EnhancedGlobalSecondaryIndex.builder()
              .indexName("GS2")
              .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
              .build();
      customerTable.createTable(
          CreateTableEnhancedRequest.builder()
              // .globalSecondaryIndices(gs1, gs2)
              .globalSecondaryIndices(gs1)
              .build());

      waitForTableCreated();

      logger.info("Table created={}", this.tableName);

      // Load sample data
      loadData(enhancedClient);
    }
  }

  private void loadData(DynamoDbEnhancedClient enhancedClient) {
    JsonResourceObjectMapper mapper = new JsonResourceObjectMapper(objectMapper);

    StopWatch watch = new StopWatch();
    watch.start();
    List<CustomerDAO> customerDAOS =
        List.of(mapper.loadFromJsonFile(CustomerDAO[].class, "test_data/customers.json"));

    for (List<CustomerDAO> customerDAOChunk : Lists.partition(customerDAOS, 25)) {
      WriteBatch.Builder<CustomerDAO> customerDAOBuilder =
          WriteBatch.builder(CustomerDAO.class).mappedTableResource(customerTable);
      customerDAOChunk.forEach(customerDAOBuilder::addPutItem);
      BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
          BatchWriteItemEnhancedRequest.builder().writeBatches(customerDAOBuilder.build()).build();
      enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }
    watch.stop();
    logger.info(
        "Loaded {} customers in {} ms", customerDAOS.size(), watch.getTime(TimeUnit.MILLISECONDS));

    watch.reset();
    watch.start();
    List<ProductDAO> productDAOS =
        List.of(mapper.loadFromJsonFile(ProductDAO[].class, "test_data/products.json"));
    for (List<ProductDAO> productDAOChunk : Lists.partition(productDAOS, 25)) {
      WriteBatch.Builder<ProductDAO> productDAOBuilder =
          WriteBatch.builder(ProductDAO.class).mappedTableResource(productTable);
      productDAOChunk.forEach(productDAOBuilder::addPutItem);
      BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
          BatchWriteItemEnhancedRequest.builder().writeBatches(productDAOBuilder.build()).build();
      enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }
    watch.stop();
    logger.info(
        "Loaded {} products in {} ms.", productDAOS.size(), watch.getTime(TimeUnit.MILLISECONDS));
  }

  void waitForTableCreated() {
    dynamoDbClient.waiter().waitUntilTableExists(b -> b.tableName(tableName));
  }

  boolean tableExists(String tableName) {
    try {
      dynamoDbClient.describeTable(r -> r.tableName(tableName));
      return true;
    } catch (ResourceNotFoundException e) {
      return false;
    }
  }

  public QueryResponse query(QueryRequest queryRequest) {
    return dynamoDbClient.query(queryRequest);
  }

  public QueryRequest.Builder queryRequestBuilder() {
    return QueryRequest.builder().tableName(tableName);
  }

  
}
