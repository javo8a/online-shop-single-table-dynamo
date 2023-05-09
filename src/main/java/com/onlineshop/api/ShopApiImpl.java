package com.onlineshop.api;

import com.onlineshop.api.dto.*;
import com.onlineshop.core.OrderCreate;
import com.onlineshop.core.ShopService;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class ShopApiImpl implements ShopApi {

  private ShopService shopService;
  private ShopDTOMapper shopDTOMapper;
  private ShopProjection shopProjection;

  @Override
  public OrderDTO createOrder(OrderCreateDTO orderCreateDTO) {
    OrderCreate order = shopDTOMapper.fromOrderCreatedDto(orderCreateDTO);
    return shopDTOMapper.toOrderDTO(shopService.createOrder(order));
  }

  @Override
  public PagedOrdersDTO getAllOrders(String lastKey, Integer pageSize) {
    return shopProjection.getAllOrders(lastKey, pageSize);
  }

  @Override
  public CustomerDTO getCustomer(UUID customerId) {
    return shopProjection.getCustomer(customerId).orElseThrow(() -> new NoSuchElementException("CustomerId not found"));
  }

  @Override
  public PagedCustomersDTO getCustomers(String lastKey, Integer pageSize) {
    return shopProjection.getCustomers(lastKey, Optional.ofNullable(pageSize).orElse(10));
  }

  @Override
  public PagedOrdersDTO getOrdersForCustomer(UUID customerId, String lastKey, Integer pageSize) {
    return shopProjection.getOrdersForCustomer(customerId, lastKey, pageSize);
  }

  @Override
  public ProductDTO getProduct(UUID productId) {
    return shopProjection.getProduct(productId).orElseThrow(() -> new NoSuchElementException("ProductId not found"));
  }

  @Override
  public PagedProductsDTO getProducts(String lastKey, Integer pageSize) {
    return shopProjection.getProducts(lastKey, Optional.ofNullable(pageSize).orElse(10));
  }

  @Override
  public OrderDetailedDTO getSingleOrder(UUID orderId) {
    return shopProjection.getOrderDetailed(orderId).orElseThrow(() -> new NoSuchElementException("OrderId not found"));
  }
}
