package com.onlineshop.api;

import com.onlineshop.api.dto.*;

import java.util.Optional;
import java.util.UUID;

public interface ShopProjection {

  PagedOrdersDTO getAllOrders(String lastKey, Integer pageSize);

  Optional<CustomerDTO> getCustomer(UUID customerId);

  PagedCustomersDTO getCustomers(String lastKey, Integer pageSize);

  PagedOrdersDTO getOrdersForCustomer(UUID customerId, String lastKey, Integer pageSize);

    Optional<ProductDTO> getProduct(UUID string);

  PagedProductsDTO getProducts(String lastKey, Integer pageSize);

    Optional<OrderDetailedDTO> getOrderDetailed(UUID orderId);
}
