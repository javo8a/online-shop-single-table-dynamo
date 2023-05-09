package com.onlineshop.infrastructure;

import com.onlineshop.api.dto.CustomerDTO;
import com.onlineshop.api.dto.PagedCustomersDTO;
import com.onlineshop.api.dto.PagedProductsDTO;
import com.onlineshop.api.dto.ProductDTO;
import com.onlineshop.infrastructure.dynamodblocal.DynamoDbResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@QuarkusTestResource(value = DynamoDbResource.class, restrictToAnnotatedClass = false)
class ShopProjectionImplIT {
    @Inject
    ShopProjectionImpl shopProjection;

    @Test
    void getCustomers() {
        PagedCustomersDTO pagedCustomersDTO = shopProjection.getCustomers(null, 10);
        assertThat(pagedCustomersDTO).isNotNull();
        assertThat(pagedCustomersDTO.getCustomers()).hasSize(10);
        assertThat(pagedCustomersDTO.getLastKey()).isNotEmpty();
    }


    @Test
    void getSingleCustomer() {
        UUID customerId = UUID.fromString("01b3f619-78c2-4eb0-b2c9-983a8139231b");
        CustomerDTO customerDTO = shopProjection.getCustomer(customerId).get();
        assertThat(customerDTO).isNotNull();
        assertThat(customerDTO.getId()).isEqualTo(customerId);
        assertThat(customerDTO.getName()).isEqualTo("Marisol Donovan");
        assertThat(customerDTO.getAddress())
                .extracting("street", "city", "state", "zipcode")
                .containsExactly("Division Avenue", "Nelson", "New Hampshire", "1555"
                );
    }

    @Test
    void getProducts() {
        PagedProductsDTO pagedProductsDTO = shopProjection.getProducts(null, 10);
        assertThat(pagedProductsDTO).isNotNull();
        assertThat(pagedProductsDTO.getProducts()).hasSize(10);
        assertThat(pagedProductsDTO.getLastKey()).isNotEmpty();
    }


    @Test
    void getSingleProduct() {
        UUID productId = UUID.fromString("03c15c0b-37ab-4b1a-b78a-fc01d7923163");
        ProductDTO productDTO = shopProjection.getProduct(productId).get();
        assertThat(productDTO).isNotNull();
        assertThat(productDTO.getId()).isEqualTo(productId);
        assertThat(productDTO.getName()).isEqualTo("Sarasonic amet");
        assertThat(productDTO.getPrice()).isEqualTo(new BigDecimal("24.16"));
    }
}