package com.onlineshop.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlineshop.api.ShopProjection;
import com.onlineshop.api.dto.*;
import com.onlineshop.infrastructure.dynamo.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ApplicationScoped
public class ShopProjectionImpl implements ShopProjection {

  private final QueryRunner queryRunner;
  private final ShopDAOMapper shopDAOMapper;
  private final ObjectMapper objectMapper;

  public ShopProjectionImpl(
      QueryRunner queryRunner, ShopDAOMapper shopDAOMapper, ObjectMapper objectMapper) {
    this.queryRunner = queryRunner;
    this.shopDAOMapper = shopDAOMapper;
    this.objectMapper = objectMapper;
  }

  @Override
  public PagedOrdersDTO getAllOrders(String lastKey, Integer pageSize) {
    Page<OrderDAO> firstPage =
        queryRunner.getOrderDAOPage(decodeExclusiveStartKey(lastKey), pageSize);
    return new PagedOrdersDTO()
        .orders(shopDAOMapper.fromOrderDAOsToDTOs(firstPage.items()))
        .lastKey(encodeLastEvaluatedKey(firstPage.lastEvaluatedKey()));
  }

  @Override
  public Optional<CustomerDTO> getCustomer(UUID customerId) {
    CustomerDAO customerDAO = queryRunner.getCustomerDAO(customerId);
    return Optional.ofNullable(shopDAOMapper.fromDAOtoDTO(customerDAO));
  }

  @Override
  public PagedCustomersDTO getCustomers(String lastKey, Integer pageSize) {
    Page<CustomerDAO> firstPage =
        queryRunner.getCustomerDAOPage(decodeExclusiveStartKey(lastKey), pageSize);
    // Construct the return DTO. Take the first page only (limit 1)
    return new PagedCustomersDTO()
        .customers(shopDAOMapper.fromCustomerDAOsToDTOs(firstPage.items()))
        .lastKey(encodeLastEvaluatedKey(firstPage.lastEvaluatedKey()));
  }

  @Override
  public PagedOrdersDTO getOrdersForCustomer(UUID customerId, String lastKey, Integer pageSize) {
    Page<OrderDAO> firstPage =
        queryRunner.getCustomerOrderDAOPage(customerId, decodeExclusiveStartKey(lastKey), pageSize);
    // Construct the return DTO. Take the first page only (limit 1)
    return new PagedOrdersDTO()
        .orders(shopDAOMapper.fromOrderDAOsToDTOs(firstPage.items()))
        .lastKey(encodeLastEvaluatedKey(firstPage.lastEvaluatedKey()));
  }

  @Override
  public Optional<ProductDTO> getProduct(UUID productId) {
    ProductDAO productDAO = queryRunner.getProductDAO(productId);
    return Optional.ofNullable(shopDAOMapper.fromDAOtoDTO(productDAO));
  }

  @Override
  public PagedProductsDTO getProducts(String lastKey, Integer pageSize) {
    Page<ProductDAO> firstPage =
        queryRunner.getProductDAOPage(decodeExclusiveStartKey(lastKey), pageSize);
    // Construct the return DTO. Take the first page only (limit 1)
    return new PagedProductsDTO()
        .products(shopDAOMapper.fromProductDAOsToDTOs(firstPage.items()))
        .lastKey(encodeLastEvaluatedKey(firstPage.lastEvaluatedKey()));
  }

  @Override
  public Optional<OrderDetailedDTO> getOrderDetailed(UUID orderId) {

    OrderDAO orderDAO = queryRunner.getOrderDetailedDAO(orderId);

    return Optional.ofNullable(shopDAOMapper.fromDAOtoDetailedDTO(orderDAO));
  }

  /**
   * When not reaching the end of the records due to hitting the limit the lastKey needs encoded:
   * json->base64
   */
  private String encodeLastEvaluatedKey(Map<String, AttributeValue> stringAttributeValueMap) {
    if (stringAttributeValueMap == null) return null;
    try {

      HashMap<String, AttributeValueLocal> stringAttributeValueLocalHashMap = new HashMap<>();
      stringAttributeValueMap
          .entrySet()
          .forEach(
              e ->
                  stringAttributeValueLocalHashMap.put(
                      e.getKey(), new AttributeValueLocal(e.getValue().s())));
      String json = objectMapper.writeValueAsString(stringAttributeValueLocalHashMap);

      return new String(Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_8)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * When receiving a lastKey decode base64 and deserialize from the JSON the AttributeValue map
   *
   * @param input
   * @return
   */
  private Map<String, AttributeValue> decodeExclusiveStartKey(String input) {
    try {
      if (input == null) return null;
      String json = new String(Base64.getDecoder().decode(input));

      TypeReference<HashMap<String, AttributeValueLocal>> typeRef = new TypeReference<>() {};

      HashMap<String, AttributeValueLocal> stringAttributeValueLocalHashMap =
          objectMapper.readValue(json, typeRef);

      HashMap<String, AttributeValue> attributeValueHashMap = new HashMap<>();
      stringAttributeValueLocalHashMap.entrySet().stream()
          .forEach(
              e -> {
                attributeValueHashMap.put(
                    e.getKey(), AttributeValue.builder().s(e.getValue().s).build());
              });
      return attributeValueHashMap;
    } catch (IllegalArgumentException | IOException e) {
      // in case of invalid payload
      throw new IllegalArgumentException("Invalid lastKey");
    }
  }

  /** Used to serialize/de-serialize AttributeValue needed for pagination queries */
  @NoArgsConstructor
  private static class AttributeValueLocal {
    @JsonProperty String s;

    public AttributeValueLocal(String s) {
      this.s = s;
    }
  }
}
