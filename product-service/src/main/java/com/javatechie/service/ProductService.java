package com.javatechie.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.javatechie.dto.ProductDto;
import com.javatechie.dto.ProductSyncPage;
import com.javatechie.entity.*;
import com.javatechie.repository.*;
import org.example.constants.ConstantValue;
import org.example.dtos.*;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ErrorMessage;
import org.example.exception.NotFoundException;
import org.example.exception.RecordExistException;
import org.springframework.beans.factory.annotation.Autowired;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UpdateQuantityTransactionRepository updateQuantityTransactionRepository;
    @Autowired
    private ProductAttributeRepository productAttributeRepository;
    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    @Autowired
    private ProductESService productESService;

    public ProductDto createProduct(ProductDto product) {
        Product existByName = productRepository.findByName(product.getName());
        if(existByName != null){
            throw new RecordExistException(ErrorMessage.PRODUCT_NAME_IS_EXISTED);
        }
        Product pro = new Product();
        pro.setName(product.getName());
        pro.setImage(product.getImage());
        pro.setPrice(product.getPrice());
        pro.setQuantity(product.getQuantity());
        pro.setDescription(product.getDescription());
        pro.setStatus(true);
        productRepository.save(pro);
        product.setId(pro.getId());
        Map<String, String> attributes = product.getAttributes();
        saveAttributes(pro.getId(), attributes);
        productESService.indexToElastic(pro, attributes);
        return product;
    }

    public List<ProductDto> getAllProducts(SearchProductRequest request) {
        return productRepository.searchWithOperators(
                request.getName(),
                request.getNameOp(),
                request.getPrice(),
                request.getPriceOp(),
                request.getQuantity(),
                request.getQuantityOp(),
                request.getStatus(),
                request.getSortBy(),
                request.getOrderBy())
                .stream().map(ProductDto::new).toList();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.PRODUCT_NOT_FOUND));
    }

    public ProductDto updateProduct(Long id, ProductDto updatedProduct) {
        Product existingProduct = getProductById(id);
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setImage(updatedProduct.getImage());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setStatus(updatedProduct.getStatus());
        productRepository.save(existingProduct);
        Map<String, String> attributes = updatedProduct.getAttributes();
        saveAttributes(id, attributes);
        productESService.indexToElastic(existingProduct, attributes);
        return updatedProduct;
    }

    private void saveAttributes(Long productId, Map<String, String> attributes) {
        if (attributes != null) {
            // Xoá cũ
            productAttributeRepository.deleteAll(productAttributeRepository.findByProductId(productId));
            // Lưu mới
            List<ProductAttribute> list = attributes.entrySet().stream().map(entry ->
                    ProductAttribute.builder()
                            .productId(productId)
                            .attributeName(entry.getKey())
                            .attributeValue(String.valueOf(entry.getValue()))
                            .attributeType("TEXT") // or detect
                            .build()
            ).toList();
            productAttributeRepository.saveAll(list);
        }
    }

    public org.example.dtos.SearchResponse<List<ProductDto>> searchProducts(ESProductRequest request) {
        try {
            Query fullTextQuery;

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                MultiMatchQuery multiMatch = MultiMatchQuery.of(m -> m
                        .query(request.getKeyword())
                        .fields("name", "description")
                );

                fullTextQuery = Query.of(q -> q.multiMatch(multiMatch));
            } else {
                fullTextQuery = Query.of(q -> q.matchAll(m -> m));
            }

            List<Query> filters = new ArrayList<>();

            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                RangeQuery.Builder priceRange = new RangeQuery.Builder().field("price");

                if (request.getMinPrice() != null) {
                    priceRange.gte(JsonData.of(request.getMinPrice()));
                }
                if (request.getMaxPrice() != null) {
                    priceRange.lte(JsonData.of(request.getMaxPrice()));
                }

                filters.add(Query.of(q -> q.range(priceRange.build())));
            }

            if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getAttributes().entrySet()) {
                    filters.add(Query.of(q -> q.term(t -> t
                            .field("attributes." + entry.getKey())
                            .value(entry.getValue())
                    )));
                }
            }

            Query finalQuery = Query.of(q -> q.bool(b -> b
                    .must(fullTextQuery)
                    .filter(filters)
            ));

            int pageIndex = request.getPage();
            int pageSize = request.getSize();

            // Build sort (nếu có)
            List<SortOptions> sortOptions = new ArrayList<>();
            if (request.getSortBy() != null && !request.getSortBy().isBlank()) {
                SortOrder order = "desc".equalsIgnoreCase(request.getOrderBy()) ? SortOrder.Desc : SortOrder.Asc;
                sortOptions.add(SortOptions.of(s -> s.field(f -> f
                        .field(request.getSortBy())
                        .order(order)
                )));
            }

            // Execute ES search
            SearchResponse<ProductESDocument> esResponse = esClient.search(s -> {
                co.elastic.clients.elasticsearch.core.SearchRequest.Builder builder = s
                        .index(ConstantValue.PRODUCT_INDEX_NAME)
                        .query(finalQuery)
                        .from(pageIndex * pageSize)
                        .size(pageSize);

                if (!sortOptions.isEmpty()) {
                    builder.sort(sortOptions);
                }

                return builder;
            }, ProductESDocument.class);

            log.info("Searched products from Elasticsearch: {}", esResponse);

            List<ProductDto> products = esResponse.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(item -> new ProductDto(
                            item.getProductId(),
                            item.getName(),
                            item.getPrice(),
                            item.getImage(),
                            item.getQuantity(),
                            item.getDescription(),
                            item.getStatus(),
                            item.getAttributes()
                    ))
                    .toList();

            org.example.dtos.SearchResponse<List<ProductDto>> result = new org.example.dtos.SearchResponse<>();
            result.setData(products);
            result.setTotalRecords(esResponse.hits().total().value());
            result.setPageIndex(pageIndex);
            result.setPageSize(pageSize);

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to search ES", e);
        }
    }

    public void deleteProduct(Long id) {
        Product existingProduct = getProductById(id);
        existingProduct.setStatus(!existingProduct.getStatus());
        productRepository.save(existingProduct);
    }

    public Object addToCart(CartItemDto item) {
        boolean isExisted = cartItemRepository.existsByProductIdAndUserId(item.getProductId(), item.getUserId());
        if(isExisted){
            log.info("{} đã có trong giỏ hàng", item.getProductId());
            return "ok";
        }
        CartItem cartItem = new CartItem(item);
        Product p = getProductById(item.getProductId());
        cartItem.setName(p.getName());
        cartItemRepository.save(cartItem);
        return "ok";
    }

    public Object getProductByUserId(Long userId) {
        return cartItemRepository.searchByUserId(userId)
                .stream().map(this::setCartItemDto).toList();
    }

    private CartItemDto setCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setName(item.getName());
        dto.setUserId(item.getUserId());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    @Transactional
    public String decreaseStock(DecreaseStockRequest request) throws Exception {
        try{
            if(request == null || request.getCartItems() == null)
                return "";
            List<Product> products = new ArrayList<>();
            Long userId = request.getCartItems().getFirst().getUserId();
            for (CartItemDto dto : request.getCartItems()) {
                if(updateQuantityTransactionRepository
                        .existsUpdateQuantityTransactionByTransactionIdAndProductId(
                                request.getTransactionId(),
                                dto.getProductId()
                                )){
                    continue;
                }
                Product product = getProductById(dto.getProductId());
                if(product.getQuantity() == 0){
                    throw new Exception(String.format("%s đã hết sản phẩm", product.getName()));
                }
                if(product.getQuantity() < dto.getQuantity()){
                    throw new Exception(String.format("%s chỉ còn %d sản phẩm", product.getName(), product.getQuantity()));
                }
                product.setQuantity(product.getQuantity() - dto.getQuantity());
                products.add(product);
                updateQuantityInElastic(product.getId(), product.getQuantity());
                updateQuantityTransactionRepository.save(
                        new UpdateQuantityTransaction(
                                null, dto.getQuantity(), userId, product.getId(), request.getTransactionId()
                        )
                );
            }
            cartItemRepository.deleteByIds(request.getCartItems().stream()
                    .map(CartItemDto::getProductId).toList(), userId);
            productRepository.saveAll(products);

            return "";
        }catch(Exception ex){
            log.info("decreaseStock ex: {}", ex.getMessage());
            throw new Exception(ex.getMessage());
        }

    }

    public void updateQuantityInElastic(Long productId, int newQuantity) {
        try {
            UpdateResponse<ProductESDocument> response = elasticsearchClient.update(u -> u
                            .index(ConstantValue.PRODUCT_INDEX_NAME)
                            .id(String.valueOf(productId))
                            .doc(Map.of("quantity", newQuantity)),
                    ProductESDocument.class
            );

            log.info("Updated product quantity in ES. ID: {}, Result: {}", response.id(), response.result());

        } catch (Exception e) {
            log.error("Failed to update product quantity in Elasticsearch", e);
        }
    }

    @Transactional
    public CommonResponse<String> removeFromCart(Long userId, Long productId) {
        try {
            cartItemRepository.deleteByUserIdAndProductId(userId, productId);
            return CommonResponse.ok("ok");
        } catch (Exception e) {
            log.info("removeFromCart ex: {}", e.getMessage());
            return CommonResponse.notOk(e.getMessage());
        }
    }

    public ProductSyncPage getProductToSync(int limit, int offset) {
        Pageable pageable = PageRequest.of(offset, limit); // offset = page number
        Page<Product> page = productRepository.findAll(pageable);
        List<Product> products = page.getContent();

        boolean hasMore = page.hasNext();

        return new ProductSyncPage(products, hasMore);
    }

}

