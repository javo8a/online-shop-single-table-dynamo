package com.onlineshop.core;

import java.util.Optional;
import java.util.UUID;

public interface ShopRepository {

    void storeOrder(Order order);

    Optional<Product> getProduct(UUID productId);

    Optional<Customer> getCustomer(UUID id);

    Optional<Order> getOrder(UUID id);
}
