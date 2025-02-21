package com.javatechie.service;

import com.javatechie.dto.ProductDto;
import com.javatechie.dto.SearchProductRequest;
import com.javatechie.entity.CartItem;
import com.javatechie.entity.Product;
import com.javatechie.repository.CartItemRepository;
import com.javatechie.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.example.constants.ErrorMessage;
import org.example.dtos.CartItemDto;
import org.example.dtos.CommonResponse;
import org.example.exception.NotFoundException;
import org.example.exception.RecordExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@Slf4j
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartItemRepository cartItemRepository;

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
        return product;
    }

    public List<ProductDto> getAllProducts(SearchProductRequest request) {
        return productRepository.search(
                request.getStatus()
        ).stream().map(ProductDto::new).toList();
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
        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        Product existingProduct = getProductById(id);
        existingProduct.setStatus(!existingProduct.getStatus());
        productRepository.save(existingProduct);
    }

    public Object addToCart(CartItemDto item) {
        CartItem cartItem = new CartItem(item);
        getProductById(item.getProductId());
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
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    public Boolean decreaseStock(List<CartItemDto> cartItems) throws Exception {
        try{
            List<Product> products = new ArrayList<>();
            for (CartItemDto dto : cartItems) {
                Product product = getProductById(dto.getProductId());
                if(product.getQuantity() < dto.getQuantity()){
                    return false;
                }
                product.setQuantity(product.getQuantity() - dto.getQuantity());
                products.add(product);
            }
            productRepository.saveAll(products);
            return true;
        }catch(Exception ex){
            log.info("decreaseStock ex: {}", ex.getMessage());
            throw new Exception(ex.getMessage());
        }

    }
}

