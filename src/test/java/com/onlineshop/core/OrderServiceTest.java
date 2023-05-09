package com.onlineshop.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit test not using CDI */
public class OrderServiceTest {

  static ShopService shopService;
  private static UUID productId = UUID.fromString("43e51c47-c953-485c-a752-189c48f03b8e");
  private static UUID customerId = UUID.fromString("96792822-8dab-4486-a272-c6f8b5a5716c");

  @BeforeAll
  public static void setupRepoMock() {
    ShopRepository shopRepositoryMock = Mockito.mock(ShopRepository.class);
    Mockito.doNothing().when(shopRepositoryMock).storeOrder(any());

    Mockito.doReturn(Optional.of(new Product(productId, "Random name", new BigDecimal(5))))
        .when(shopRepositoryMock)
        .getProduct(any());

    Mockito.doReturn(
            Optional.of(
                Customer.builder()
                    .id(customerId)
                    .name("Customer name")
                    .address(new Customer.Address("Street", "City", "State", "12345"))
                    .build()))
        .when(shopRepositoryMock)
        .getCustomer(any());
    shopService = new ShopServiceImpl(shopRepositoryMock);
  }

  @Test
  public void createOrder() {

    OrderCreate orderCreate =
        new OrderCreate(customerId, List.of(new OrderCreate.OrderCreateItem(productId, 7)));
    Order order = shopService.createOrder(orderCreate);
    assertThat(order).isNotNull();
    assertThat(order.getId()).isNotNull();
    assertThat(order.getCustomer()).isNotNull();
    assertThat(order.getItems().get(0)).extracting("productId").isEqualTo(productId);
    assertThat(order.getTotal()).isEqualTo(new BigDecimal(35));
  }
}
