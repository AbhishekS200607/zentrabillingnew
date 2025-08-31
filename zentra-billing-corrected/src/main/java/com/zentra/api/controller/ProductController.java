package com.zentra.api.controller;

import com.zentra.api.entity.Product;
import com.zentra.api.service.ProductService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

  @Autowired private ProductService productService;

  @GetMapping
  public List<Product> getAllProducts() {
    return productService.getAllProducts();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return productService
        .getProductById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public Product createProduct(@RequestBody Product product) {
    return productService.saveProduct(product);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Product> updateProduct(
      @PathVariable Long id, @RequestBody Product productDetails) {
    return productService
        .getProductById(id)
        .map(
            existingProduct -> {
              // **FIX: Manually update fields from the request to preserve the version**
              existingProduct.setName(productDetails.getName());
              existingProduct.setCategory(productDetails.getCategory());
              existingProduct.setBarcode(productDetails.getBarcode());
              existingProduct.setCostPrice(productDetails.getCostPrice());
              existingProduct.setSellingPrice(productDetails.getSellingPrice());
              existingProduct.setPrice(productDetails.getSellingPrice());
              existingProduct.setStock(productDetails.getStock());
              // Let the service handle the updatedAt timestamp
              return ResponseEntity.ok(productService.saveProduct(existingProduct));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/search")
  public List<Product> searchProducts(@RequestParam String q) {
    return productService.searchProducts(q);
  }

  @GetMapping("/low-stock")
  public List<Product> getLowStockProducts() {
    return productService.getLowStockProducts();
  }

  @PostMapping("/{id}/update-stock")
  public ResponseEntity<Void> updateStock(
      @PathVariable Long id, @RequestParam Integer quantity) {
    boolean success = productService.updateStock(id, quantity);
    if (success) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }
}