package com.onlineshop.core;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Customer {
    UUID id;
    String name;

    Address address;

    OffsetDateTime lastOrder;

    public static record Address (String street, String city, String state, String zipcode) {
    }
}
