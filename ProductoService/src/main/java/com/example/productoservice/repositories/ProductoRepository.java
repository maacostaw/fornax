package com.example.productoservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.productoservice.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
