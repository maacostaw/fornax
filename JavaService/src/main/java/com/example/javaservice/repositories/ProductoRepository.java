package com.example.javaservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.javaservice.entities.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
