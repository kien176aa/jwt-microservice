package com.javatechie.entity;

import lombok.*;
import org.example.constants.ConstantValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Document(indexName = ConstantValue.PRODUCT_INDEX_NAME)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductESDocument {
    @Id
    private String id;

    private Long productId;
    private String name;
    private Double price;
    private String image;
    private Integer quantity;
    private String description;
    private Boolean status;

    @Field(type = FieldType.Object)
    private Map<String, String> attributes;
}

