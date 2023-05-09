package com.onlineshop.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.core.Order;
import com.onlineshop.infrastructure.dynamo.OrderDAO;
import com.onlineshop.infrastructure.dynamodblocal.DynamoDbResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = DynamoDbResource.class, restrictToAnnotatedClass = false)
public class ShopRepositoryImplIT {

  @Inject ShopRepositoryImpl shopRepository;
  @Inject ObjectMapper objectMapper;
  @Inject ShopDAOMapper shopDAOMapper;
  private OrderDAO orderDAO;

  @BeforeEach
  public void setup() throws IOException {
    orderDAO =
        objectMapper.readValue(
            """
                {
                    "customer": {
                      "customerId": "8482a644-77db-486b-8816-10f98ecd7838",
                      "orderId": "3a936cb0-5458-40d4-975a-e07929d2d4e0",
                      "name": "John Smith",
                      "address": {
                             "street": "Denton Place",
                             "city": "Rodman",
                             "state": "Missouri",
                             "zipcode": 35574
                           }
                    },
                    "items": [
                        {
                            "orderId": "3a936cb0-5458-40d4-975a-e07929d2d4e0",
                            "productId": "05a14762-e9e1-4e9d-a048-f4ec3359e31b",
                            "name": "the product",
                            "price": "10",
                            "quantity": 2
                        }
                    ],
                    "id": "3a936cb0-5458-40d4-975a-e07929d2d4e0",
                    "total": 424.74,
                    "lastUpdated": "2023-05-07T15:43:32.750871-04:00"
                }
                """,
            OrderDAO.class);
  }

  @Test()
  void testStoreOrder() {
    Order order = shopDAOMapper.fromDAOtoDomain(orderDAO);

    shopRepository.storeOrder(order);

    Order result = shopRepository.getOrder(order.getId()).get();

    assertThat(result).isNotNull();
    assertThat(order)
        .extracting("id", "total")
        .containsExactly(
            UUID.fromString("3a936cb0-5458-40d4-975a-e07929d2d4e0"), new BigDecimal("424.74"));
  }
}
