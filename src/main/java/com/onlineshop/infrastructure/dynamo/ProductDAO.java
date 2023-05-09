package com.onlineshop.infrastructure.dynamo;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.UuidAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

@Data
public class ProductDAO {

    public static final String ENTITY_TYPE = "Product";
    static final BiConsumer<ProductDAO, String> NO_OP = (__, ___) -> {
    };
    public final static String PRODUCT_PREFIX = "p#";
    static final Function<ProductDAO, String> PK_OP = (p) -> PRODUCT_PREFIX + p.getId();
    static final Function<ProductDAO, String> SK_OP = (p) -> PRODUCT_PREFIX;
    static final Function<ProductDAO, String> GS1_PK_OP = (c) -> "product";
    static final Function<ProductDAO, String> GS1_SK_OP = (c) -> PRODUCT_PREFIX + c.getId(); // with scan_index_forward should always come first

    static final Function<ProductDAO, String> GET_TYPE_OP = (product) -> ENTITY_TYPE;
    static final BiConsumer<ProductDAO, String> SET_TYPE_OP = (product, type) -> {
        if (!ENTITY_TYPE
                .equals(type)) {
            throw new IllegalArgumentException(
                    "Attempted to marshall into ProductDAO a row of Type=" + type);
        }
    };


    public static String prefixedId(UUID id) {
        return PRODUCT_PREFIX + id;
    }

    private UUID id;
    private String name;
    private BigDecimal price;


    public static TableSchema<ProductDAO> schema() {
        return StaticTableSchema.builder(ProductDAO.class)
                .newItemSupplier(ProductDAO::new)
                .addAttribute(String.class,
                        a -> a.name("PK")
                                .getter(PK_OP)
                                .setter(NO_OP)
                                .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(String.class,
                        a -> a.name("SK")
                                .getter(SK_OP)
                                .setter(NO_OP)
                                .tags(StaticAttributeTags.primarySortKey()))
                .addAttribute(String.class,
                        a -> a.name("type")
                                .getter(GET_TYPE_OP)
                                .setter(SET_TYPE_OP))
                .addAttribute(UUID.class,
                        a -> a.name("id")
                                .attributeConverter(UuidAttributeConverter.create())
                                .getter(ProductDAO::getId)
                                .setter(ProductDAO::setId))
                .addAttribute(String.class,
                        a -> a.name("name")
                                .getter(ProductDAO::getName)
                                .setter(ProductDAO::setName))
                .addAttribute(BigDecimal.class,
                        a -> a.name("price")
                                .getter(ProductDAO::getPrice)
                                .setter(ProductDAO::setPrice))
                .addAttribute(String.class, a -> a.name("GS1PK")
                        .getter(GS1_PK_OP)
                        .setter(NO_OP)
                        .tags(StaticAttributeTags
                                .secondaryPartitionKey("GS1")))
                .addAttribute(String.class, a -> a.name("GS1SK")
                        .getter(GS1_SK_OP)
                        .setter(NO_OP)
                        .tags(StaticAttributeTags.secondarySortKey("GS1")))
                .build();
    }


}
