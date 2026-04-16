package com.demo.product.controller;

import com.demo.product.entity.Product;
import com.demo.product.repository.ProductRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author TangZhikai
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'EDITOR', 'PRODUCT_ADMIN')")
    public List<Product> list() {
        return productRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDITOR', 'PRODUCT_ADMIN')")
    public Product add(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'PRODUCT_ADMIN')")
    public Product modify(@PathVariable(value = "id") Long id, @RequestBody Product productDetails) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        product.setName(productDetails.getName());
        return productRepository.save(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'PRODUCT_ADMIN')")
    public void delete(@PathVariable(value = "id") Long id) {
        productRepository.deleteById(id);
    }
}
