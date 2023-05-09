package com.onlineshop.api;

import com.onlineshop.api.dto.OrderCreateDTO;
import com.onlineshop.api.dto.OrderCreateItemsDTO;
import com.onlineshop.api.dto.OrderDTO;
import com.onlineshop.core.Order;
import com.onlineshop.core.OrderCreate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public abstract class ShopDTOMapper {

    /*
     * DTO <-> Domain
     */
    abstract OrderCreate fromOrderCreatedDto(OrderCreateDTO orderCreateDTO);

    protected abstract OrderCreate.OrderCreateItem fromOrderCreateItemDto(OrderCreateItemsDTO orderCreateItemsDTO);

    @Mapping(source="customer.id",target = "customerId")
    abstract OrderDTO toOrderDTO(Order order);



}
