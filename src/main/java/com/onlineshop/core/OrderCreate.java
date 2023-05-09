package com.onlineshop.core;

import java.util.List;
import java.util.UUID;

public record OrderCreate(UUID customerId, List<OrderCreateItem> items) {
    public record OrderCreateItem(UUID productId, Integer quantity) {
    }
}
