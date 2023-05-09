package com.onlineshop.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.onlineshop.core.*;
import com.onlineshop.infrastructure.dynamo.OrderDAO;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

class ShopDAOMapperTest {

  ShopDAOMapper shopDAOMapper = Mappers.getMapper(ShopDAOMapper.class);

  UUID productId = UUID.fromString("43e51c47-c953-485c-a752-189c48f03b8e");
  UUID customerId = UUID.fromString("96792822-8dab-4486-a272-c6f8b5a5716c");

  ShopService shopService;

  @Test
  void fromDomainToDAO() {

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

    OrderCreate orderCreate =
        new OrderCreate(customerId, List.of(new OrderCreate.OrderCreateItem(productId, 7)));
    Order order = shopService.createOrder(orderCreate);

    OrderDAO orderDAO = shopDAOMapper.fromDomainToDAO(order);

    assertThat(orderDAO).isNotNull();
    assertThat(orderDAO.getCustomerId()).isNotNull();
    assertThat(orderDAO.getCustomer()).isNotNull();
    assertThat(orderDAO.getCustomer()).extracting("orderId","customerId","name").containsExactly(order.getId(),customerId,"Customer name");
    assertThat(orderDAO.getItems()).hasSize(1);
    assertThat(orderDAO.getItems().get(0))
        .extracting("orderId", "productId", "name", "price")
        .containsExactly(order.getId(), productId, "Random name", new BigDecimal(5));
  }

  @Test
  void fromDAOtoDetailedDTO() {}
}
