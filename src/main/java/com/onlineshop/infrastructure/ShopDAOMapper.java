package com.onlineshop.infrastructure;

import com.onlineshop.api.dto.CustomerDTO;
import com.onlineshop.api.dto.OrderDTO;
import com.onlineshop.api.dto.OrderDetailedDTO;
import com.onlineshop.api.dto.ProductDTO;
import com.onlineshop.core.Customer;
import com.onlineshop.core.Order;
import com.onlineshop.core.OrderItem;
import com.onlineshop.core.Product;
import com.onlineshop.infrastructure.dynamo.*;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "cdi")
public interface ShopDAOMapper {

  List<CustomerDTO> fromCustomerDAOsToDTOs(List<CustomerDAO> items);

  CustomerDTO fromDAOtoDTO(CustomerDAO customerDAO);

  @Mapping(source = "customerId", target = "id")
  @Mapping(target = "lastOrder", ignore = true)
  CustomerDTO fromDAOtoDTO(CustomerOrderDAO customerDAO);

  List<ProductDTO> fromProductDAOsToDTOs(List<ProductDAO> items);

  ProductDTO fromDAOtoDTO(ProductDAO productDAO);

  // Domain <-> DAO

  @Mapping(source = "customer.id", target = "customerId")
  @Mapping(source = "customer.id", target = "customer.customerId")
  @Mapping(source = "id", target = "customer.orderId")
  @Mapping(source = ".", target = "items")
  OrderDAO fromDomainToDAO(Order order);

  CustomerOrderDAO.AddressDAO fromDomainToDAO(Customer.Address value);

  default List<OrderItemDAO> getOrderItems(Order order) {
    List<OrderItemDAO> orderItemDAOS =
        order.getItems().stream().map(this::fromDomainToDAO).toList();
    orderItemDAOS.forEach(oi -> oi.setOrderId(order.getId()));
    return orderItemDAOS;
  }

  @Mapping(target = "orderId", ignore = true)
  OrderItemDAO fromDomainToDAO(OrderItem value);

  Product fromDAOtoDomain(ProductDAO productDAO);

  Customer fromDAOtoDomain(CustomerDAO productDAO);

  @Mapping(source = "customer.customerId", target = "customer.id")
  @Mapping(target = "customer.lastOrder", ignore = true)
  Order fromDAOtoDomain(OrderDAO orderDAO);

  //  @Mapping(source = "productId", target = "id")
  OrderItem fromDAOtoDomain(OrderItemDAO orderItemDAO);

  List<OrderDTO> fromOrderDAOsToDTOs(List<OrderDAO> items);

  OrderDetailedDTO fromDAOtoDetailedDTO(OrderDAO orderDAO);

  OrderDTO fromDAOtoDTO(OrderDAO orderDAO);
}
