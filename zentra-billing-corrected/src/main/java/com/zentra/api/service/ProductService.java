package com.zentra.api.service;

import com.zentra.api.entity.Product;
import com.zentra.api.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        if (product.getId() == null) {
            product.setCreatedAt(LocalDateTime.now());
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String search) {
        return productRepository.findByNameOrBarcodeContaining(search);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Long getLowStockCount() {
        return productRepository.countLowStockProducts();
    }

    public boolean updateStock(Long productId, Integer quantityChange) {
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                int newStock = product.getStock() - quantityChange;
                if (newStock >= 0) {
                    product.setStock(newStock);
                    saveProduct(product); 
                    return true;
                }
            }
        } catch (OptimisticLockException e) {
            // **FIX: Re-throw the exception to allow the transaction to roll back**
            System.err.println("Optimistic Lock Exception during stock update: " + e.getMessage());
            throw e; 
        }
        return false;
    }
}