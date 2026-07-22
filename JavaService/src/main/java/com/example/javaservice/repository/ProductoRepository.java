package com.example.javaservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.javaservice.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
