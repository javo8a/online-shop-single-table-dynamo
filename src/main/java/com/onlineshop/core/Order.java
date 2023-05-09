package com.onlineshop.core;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.Builder;
import lombok.Getter;

/** Aggregate root for Order. It controls creation/modification of its orderItems. */
@Builder
@RegisterForReflection
@Getter
public class Order {

  private final UUID id;
  private final List<OrderItem> items;
  private final Customer customer;
  private BigDecimal total;
  private OffsetDateTime created;
  private OffsetDateTime lastUpdated;

  public Order(UUID id, List<OrderItem> items, Customer customer, BigDecimal total, OffsetDateTime created, OffsetDateTime lastUpdated) {
    this.id = Optional.ofNullable(id).orElse(UUID.randomUUID());
    this.items = Optional.ofNullable(items).orElse(new ArrayList<>());
    this.customer = Optional.ofNullable(customer).orElseThrow(() -> new IllegalArgumentException("Customer is required"));
    this.total = Optional.ofNullable(total).orElse(BigDecimal.ZERO);
    this.created = Optional.ofNullable(created).orElse(OffsetDateTime.now());
    this.lastUpdated = Optional.ofNullable(lastUpdated).orElse(OffsetDateTime.now());
  }

  // Adding items later is allowed
  void addItem(OrderItem orderItem) {
    items.add(orderItem);
    total = total.add(orderItem.price().multiply(new BigDecimal(orderItem.quantity())));
    lastUpdated = OffsetDateTime.now();
  }

  void removeItem(UUID productId) {
    OrderItem item =
        items.stream()
            .filter(i -> i.productId().equals(productId))
            .findFirst()
            .orElseThrow(
                () -> new IllegalArgumentException("Item with this productId is not in the order"));
    items.remove(item);
    total = total.subtract(item.price().multiply(new BigDecimal(item.quantity())));
  }

  static OrderBuilder builder(ShopRepository shopRepository) {
    return new OrderBuilder().shopRepository(shopRepository);
  }

  /** Extension on the builder to enforce default values and invariants upon construction */
  static class OrderBuilder {

    private BigDecimal total = BigDecimal.ZERO;
    private List<OrderItem> items = new ArrayList<>();
    private ShopRepository shopRepository;

    public OrderBuilder customerId(UUID id) {
      UUID customerId =
          Optional.ofNullable(id)
              .orElseThrow(() -> new IllegalArgumentException("CustomerId cannot be null"));
      customer =
          shopRepository
              .getCustomer(customerId)
              .orElseThrow(() -> new NoSuchElementException("CustomerId not found"));

      return this;
    }

    // Adding items when building one order is expected
    public OrderBuilder addItems(List<OrderCreate.OrderCreateItem> orderItems) {
      for (OrderCreate.OrderCreateItem item : orderItems) {
        Product product =
            shopRepository
                .getProduct(item.productId())
                .orElseThrow(() -> new NoSuchElementException("ProductId not found"));
        OrderItem orderItem =
            new OrderItem(product.id(), product.name(), product.price(), item.quantity());
        items.add(orderItem);
        total = total.add(orderItem.price().multiply(new BigDecimal(item.quantity())));
      }

      return this;
    }

    public OrderBuilder shopRepository(ShopRepository shopRepository) {
      this.shopRepository = shopRepository;
      return this;
    }
  }
}
