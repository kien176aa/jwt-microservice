package com.javatechie.batch;

import com.javatechie.entity.Product;
import com.javatechie.entity.ProductAttribute;
import com.javatechie.repository.ProductAttributeRepository;
import com.javatechie.service.ProductESService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@Slf4j
public class SyncProductToES implements Runnable {

    @Autowired
    private ProductAttributeRepository productAttributeRepository;
    @Autowired
    private ProductESService productESService;
    @Setter
    private List<Product> products = new ArrayList<>();

    @Override
    public void run() {
        try {
            for (Product product : products) {
                productESService.indexToElastic(product, getProductAtrr(product.getId()));
            }
        } catch (Exception e) {
            log.error("Error while syncing products: ", e);
        }
    }

    private Map<String, String> getProductAtrr(Long id) {
        List<ProductAttribute> attributes = productAttributeRepository.findByProductId(id);

        return attributes.stream()
                .collect(Collectors.toMap(
                        ProductAttribute::getAttributeName,
                        attr -> attr.getAttributeValue() != null ? attr.getAttributeValue() : ""
                ));
    }
}
