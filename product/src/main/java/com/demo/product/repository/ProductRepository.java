package com.demo.product.repository;

import com.demo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author TangZhikai
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
