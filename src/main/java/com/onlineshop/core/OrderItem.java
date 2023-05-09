package com.onlineshop.core;

import io.quarkus.runtime.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(UUID productId, String name, BigDecimal price, Integer quantity) {

    public OrderItem(UUID productId, String name, BigDecimal price, Integer quantity) {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("Product name cannot be empty");
        if (productId==null)
            throw new IllegalArgumentException("Product id cannot be empty");
        if (price == null)
            throw new IllegalArgumentException("Product price cannot be empty");
        if (quantity==null || quantity.compareTo(0)<=0)
            throw new IllegalArgumentException("Quantity needs to be 1 or more");

        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}


