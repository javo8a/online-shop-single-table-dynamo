package com.onlineshop.api;

import com.onlineshop.api.dto.OrderCreateDTO;
import com.onlineshop.api.dto.OrderCreateItemsDTO;
import com.onlineshop.core.OrderCreate;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShopDTOMapperTest {
    ShopDTOMapper shopDTOMapper = Mappers.getMapper(ShopDTOMapper.class);

    @Test
    public void orderCreateMapperTest() {
        OrderCreateDTO orderCreatedDto = new OrderCreateDTO();
        UUID customerId = UUID.randomUUID();
        orderCreatedDto.setCustomerId(customerId);

        OrderCreateItemsDTO orderItemDTO = new OrderCreateItemsDTO();
        UUID productId = UUID.randomUUID();
        orderItemDTO.setProductId(productId);
        orderItemDTO.setQuantity(7);

        orderCreatedDto.getItems().add(orderItemDTO);

        OrderCreate orderCreate = shopDTOMapper.fromOrderCreatedDto(orderCreatedDto);

        assertThat(orderCreate).isNotNull();
        assertThat(orderCreate.customerId()).isEqualTo(customerId);
        assertThat(orderCreate.items()).hasSize(1);
        assertThat(orderCreate.items().get(0)).extracting("productId", "quantity").containsExactly(productId, 7);
    }


//    @Test
//    void toOrderDTO() {
//        UUID customerId = UUID.randomUUID();
//        UUID productId = UUID.randomUUID();
//        OrderCreate orderCreate = new OrderCreate(customerId, List.of(new OrderCreate.OrderCreateItem(productId, 7)));
//
//        Order order = Order.builder().customerId(customerId).addItems()
//
//                build();
//    }
}