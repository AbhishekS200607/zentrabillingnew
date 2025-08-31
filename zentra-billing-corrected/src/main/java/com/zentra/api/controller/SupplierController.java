package com.zentra.api.controller;
import com.zentra.api.entity.Product;
import com.zentra.api.entity.Supplier;
import com.zentra.api.repository.ProductRepository;
import com.zentra.api.repository.SupplierRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    // Using constructor injection for better dependency management
    public SupplierController(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Correctly sets the creation timestamp before saving
    @PostMapping
    public Supplier createSupplier(@RequestBody Supplier supplier) {
        supplier.setCreatedAt(LocalDateTime.now());
        return supplierRepository.save(supplier);
    }

    // Correctly updates only the intended fields
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplierDetails) {
        return supplierRepository.findById(id)
                .map(existingSupplier -> {
                    existingSupplier.setName(supplierDetails.getName());
                    existingSupplier.setPhone(supplierDetails.getPhone());
                    existingSupplier.setEmail(supplierDetails.getEmail());
                    existingSupplier.setAddress(supplierDetails.getAddress());
                    Supplier updatedSupplier = supplierRepository.save(existingSupplier);
                    return ResponseEntity.ok(updatedSupplier);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<Map<String, Object>> getSupplierAnalytics(@PathVariable Long id) {
        return supplierRepository.findById(id)
                .map(supplier -> {
                    Map<String, Object> analytics = new HashMap<>();
                    List<Product> products = productRepository.findByDistributorId(id);
                    analytics.put("totalProducts", products.size());
                    // Further analytics can be added here
                    return ResponseEntity.ok(analytics);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
