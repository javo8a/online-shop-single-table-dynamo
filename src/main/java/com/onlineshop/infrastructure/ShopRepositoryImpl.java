package com.onlineshop.infrastructure;

import com.onlineshop.core.Customer;
import com.onlineshop.core.Order;
import com.onlineshop.core.Product;
import com.onlineshop.core.ShopRepository;
import com.onlineshop.infrastructure.dynamo.*;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

@ApplicationScoped
public class ShopRepositoryImpl implements ShopRepository {

  private final ShopDAOMapper shopDAOMapper;
  private final DynamoDbEnhancedClient enhancedClient;
  private final QueryRunner queryRunner;
  public final DynamoDbTable<OrderDAO> orderTable;
  public final DynamoDbTable<CustomerOrderDAO> customerOrderTable;
  public final DynamoDbTable<OrderItemDAO> orderItemTable;

  public ShopRepositoryImpl(
      ShopDAOMapper shopDAOMapper, DynamoDBHandler dynamoDBHandler, QueryRunner queryRunner) {
    this.shopDAOMapper = shopDAOMapper;
    this.enhancedClient = dynamoDBHandler.getEnhancedClient();
    this.queryRunner = queryRunner;
    this.orderTable = dynamoDBHandler.getOrderTable();
    this.customerOrderTable = dynamoDBHandler.getCustomerOrderTable();
    this.orderItemTable = dynamoDBHandler.getOrderItemTable();
  }

  @Override
  public void storeOrder(Order order) {
    OrderDAO orderDAO = shopDAOMapper.fromDomainToDAO(order);

    BatchWriteItemEnhancedRequest.Builder batchWriteItemEnhancedRequest =
        BatchWriteItemEnhancedRequest.builder()
            .addWriteBatch(
                WriteBatch.builder(OrderDAO.class)
                    .mappedTableResource(orderTable)
                    .addPutItem(orderDAO)
                    .build());

    batchWriteItemEnhancedRequest.addWriteBatch(
        WriteBatch.builder(CustomerOrderDAO.class)
            .mappedTableResource(customerOrderTable)
            .addPutItem(orderDAO.getCustomer())
            .build());

    for (OrderItemDAO item : orderDAO.getItems())
      batchWriteItemEnhancedRequest.addWriteBatch(
          WriteBatch.builder(OrderItemDAO.class)
              .mappedTableResource(orderItemTable)
              .addPutItem(item)
              .build());

    enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest.build());
  }

  @Override
  public Optional<Product> getProduct(UUID productId) {
    return Optional.of(shopDAOMapper.fromDAOtoDomain(queryRunner.getProductDAO(productId)));
  }

  @Override
  public Optional<Customer> getCustomer(UUID id) {
    return Optional.of(shopDAOMapper.fromDAOtoDomain(queryRunner.getCustomerDAO(id)));
  }

  @Override
  public Optional<Order> getOrder(UUID id) {
    OrderDAO orderDAO = queryRunner.getOrderDetailedDAO(id);
    return Optional.of(shopDAOMapper.fromDAOtoDomain(orderDAO));
  }
}
