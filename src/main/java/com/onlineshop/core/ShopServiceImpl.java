package com.onlineshop.core;

import javax.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class ShopServiceImpl implements ShopService {

    private ShopRepository shopRepository;

    @Override
    public Order createOrder(OrderCreate orderCreate) {

        // Build a consistent order with items and prices
        Order order = Order.builder(shopRepository)
                .customerId(orderCreate.customerId())
                .addItems(orderCreate.items())
                .build();

        shopRepository.storeOrder(order);

        return order;
    }
}
