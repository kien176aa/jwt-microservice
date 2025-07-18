package com.javatechie.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.javatechie.entity.Product;
import com.javatechie.entity.ProductESDocument;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ConstantValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ProductESService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    public void deleteAllProductsFromES() {
        try {
            DeleteByQueryRequest request = DeleteByQueryRequest.of(b -> b
                    .index(ConstantValue.PRODUCT_INDEX_NAME) // <-- thay bằng tên index thực tế của bạn
                    .query(q -> q.matchAll(m -> m)) // xóa tất cả documents
            );

            elasticsearchClient.deleteByQuery(request);
        } catch (Exception e) {
            log.error("Failed to delete all products from Elasticsearch", e);
        }
    }

    public void indexToElastic(Product product, Map<String, String> attributes) {
        try {
            StringBuilder descBuilder = new StringBuilder();

            descBuilder.append(product.getName()).append(" ");

            if (product.getDescription() != null) {
                descBuilder.append(product.getDescription()).append(" ");
            }

            if (attributes != null) {
                attributes.forEach((k, v) -> {
                    descBuilder.append(v).append(" ");
                });
            }
            ProductESDocument doc = ProductESDocument.builder()
                    .id(String.valueOf(product.getId()))
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .image(product.getImage())
                    .quantity(product.getQuantity())
                    .description(descBuilder.toString().trim())
                    .status(product.getStatus())
                    .attributes(attributes)
                    .build();

            IndexRequest<ProductESDocument> request = IndexRequest.of(i -> i
                    .index(ConstantValue.PRODUCT_INDEX_NAME)
                    .id(doc.getId())
                    .document(doc)
            );

            IndexResponse response = elasticsearchClient.index(request);
            log.info("Indexed document id: {}", response.id());

        } catch (Exception e) {
            log.error("Failed to index product to Elasticsearch", e);
        }
    }

}
